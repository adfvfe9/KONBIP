package com.konbip.auth.oauth;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import com.konbip.auth.AuthService;
import com.konbip.auth.dto.TokenResponse;
import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;
import com.konbip.global.security.JwtTokenProvider;
import com.konbip.user.AuthProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Google / Kakao OAuth2 Authorization Code 흐름.
 * <ol>
 *   <li>프론트가 GET /api/auth/oauth2/{provider} 로 이동 → 제공자 로그인 화면으로 302</li>
 *   <li>제공자가 GET /api/auth/oauth2/callback/{provider}?code=..&state=.. 로 돌려보냄</li>
 *   <li>서버가 code 로 토큰/회원정보를 받아 가입·로그인 처리</li>
 *   <li>프론트 주소로 302: {frontend}#accessToken=..&refreshToken=..&newUser=true|false
 *       (실패 시 {frontend}#error=OAUTH_FAILED 같은 코드)</li>
 * </ol>
 * 토큰을 쿼리스트링이 아닌 URL fragment(#)로 넘겨서 서버 로그나 Referer 에 남지 않게 했다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

	private static final ParameterizedTypeReference<Map<String, Object>> JSON_MAP = new ParameterizedTypeReference<>() {
	};

	private final OAuthProperties properties;
	private final JwtTokenProvider jwtTokenProvider;
	private final SocialAccountService socialAccountService;
	private final AuthService authService;
	private final RestClient restClient = RestClient.create();

	public URI authorizationUri(String providerKey) {
		AuthProvider provider = AuthProvider.from(providerKey);
		OAuthProperties.Provider config = config(provider);
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(config.authorizationUri())
				.queryParam("response_type", "code")
				.queryParam("client_id", config.clientId())
				.queryParam("redirect_uri", redirectUri(provider))
				.queryParam("state", jwtTokenProvider.createOAuthState(provider.key()));
		if (config.scope() != null && !config.scope().isBlank()) {
			builder.queryParam("scope", config.scope());
		}
		return builder.encode().build().toUri();
	}

	public URI handleCallback(String providerKey, String code, String state, String error) {
		try {
			AuthProvider provider = AuthProvider.from(providerKey);
			if (error != null || code == null || code.isBlank()) {
				throw new BusinessException(ErrorCode.OAUTH_FAILED);
			}
			if (!jwtTokenProvider.isValidOAuthState(state, provider.key())) {
				throw new BusinessException(ErrorCode.OAUTH_FAILED);
			}
			OAuthProperties.Provider config = config(provider);
			String providerAccessToken = exchangeCode(provider, config, code);
			OAuthUserInfo userInfo = fetchUserInfo(provider, config, providerAccessToken);
			SocialAccountService.Result result = socialAccountService.findOrCreate(provider, userInfo);
			TokenResponse tokens = authService.issueTokens(result.user());

			Map<String, String> fragment = new LinkedHashMap<>();
			fragment.put("accessToken", tokens.accessToken());
			fragment.put("refreshToken", tokens.refreshToken());
			fragment.put("newUser", String.valueOf(result.newUser()));
			return frontendRedirect(fragment);
		} catch (BusinessException e) {
			return errorRedirect(e.getErrorCode());
		} catch (RestClientException e) {
			log.warn("OAuth provider call failed: {}", e.getMessage());
			return errorRedirect(ErrorCode.OAUTH_FAILED);
		}
	}

	public URI errorRedirect(ErrorCode errorCode) {
		return frontendRedirect(Map.of("error", errorCode.name()));
	}

	private String exchangeCode(AuthProvider provider, OAuthProperties.Provider config, String code) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "authorization_code");
		form.add("code", code);
		form.add("redirect_uri", redirectUri(provider));
		form.add("client_id", config.clientId());
		if (config.clientSecret() != null && !config.clientSecret().isBlank()) {
			form.add("client_secret", config.clientSecret());
		}
		Map<String, Object> body = restClient.post()
				.uri(config.tokenUri())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(JSON_MAP);
		Object token = body == null ? null : body.get("access_token");
		if (!(token instanceof String accessToken) || accessToken.isBlank()) {
			throw new BusinessException(ErrorCode.OAUTH_FAILED);
		}
		return accessToken;
	}

	private OAuthUserInfo fetchUserInfo(AuthProvider provider, OAuthProperties.Provider config, String accessToken) {
		Map<String, Object> body = restClient.get()
				.uri(config.userInfoUri())
				.headers(headers -> headers.setBearerAuth(accessToken))
				.retrieve()
				.body(JSON_MAP);
		if (body == null) {
			throw new BusinessException(ErrorCode.OAUTH_FAILED);
		}
		OAuthUserInfo info = switch (provider) {
			case GOOGLE -> new OAuthUserInfo(
					asString(body.get("sub")),
					Boolean.TRUE.equals(body.get("email_verified")) ? asString(body.get("email")) : null);
			case KAKAO -> {
				Object account = body.get("kakao_account");
				String email = null;
				if (account instanceof Map<?, ?> kakaoAccount && Boolean.TRUE.equals(kakaoAccount.get("is_email_verified"))) {
					email = asString(kakaoAccount.get("email"));
				}
				yield new OAuthUserInfo(asString(body.get("id")), email);
			}
		};
		if (info.providerUserId() == null || info.providerUserId().isBlank()) {
			throw new BusinessException(ErrorCode.OAUTH_FAILED);
		}
		return info;
	}

	private OAuthProperties.Provider config(AuthProvider provider) {
		OAuthProperties.Provider config = properties.providers() == null ? null
				: properties.providers().get(provider.key());
		if (config == null || !config.isConfigured()) {
			throw new BusinessException(ErrorCode.OAUTH_PROVIDER_NOT_CONFIGURED);
		}
		return config;
	}

	private String redirectUri(AuthProvider provider) {
		return properties.redirectBaseUri() + "/api/auth/oauth2/callback/" + provider.key();
	}

	private URI frontendRedirect(Map<String, String> fragment) {
		String encoded = fragment.entrySet().stream()
				.map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
				.collect(Collectors.joining("&"));
		return URI.create(properties.frontendRedirectUri() + "#" + encoded);
	}

	private static String asString(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
