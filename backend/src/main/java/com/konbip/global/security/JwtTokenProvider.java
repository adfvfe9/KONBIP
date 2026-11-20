package com.konbip.global.security;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;
import com.konbip.user.Role;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * HS256 JWT 발급/검증.
 * <ul>
 *   <li>access: API 호출용. Authorization: Bearer {accessToken}</li>
 *   <li>refresh: POST /api/auth/refresh 로 새 토큰을 받을 때만 사용</li>
 *   <li>oauth_state: 소셜 로그인 CSRF 방지용 state 값</li>
 * </ul>
 * 토큰 종류는 token_type 클레임으로 구분해서 refresh 토큰을 access 토큰처럼 쓸 수 없게 한다.
 */
@Component
public class JwtTokenProvider {

	public static final String CLAIM_TOKEN_TYPE = "token_type";
	public static final String CLAIM_ROLE = "role";
	private static final String CLAIM_PROVIDER = "provider";

	private static final String TYPE_ACCESS = "access";
	private static final String TYPE_REFRESH = "refresh";
	private static final String TYPE_OAUTH_STATE = "oauth_state";
	private static final Duration OAUTH_STATE_VALIDITY = Duration.ofMinutes(10);

	private final JwtProperties properties;
	private final JwtEncoder encoder;
	private final JwtDecoder accessTokenDecoder;
	private final JwtDecoder refreshTokenDecoder;
	private final JwtDecoder oauthStateDecoder;

	public JwtTokenProvider(JwtProperties properties) {
		this.properties = properties;
		this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(properties.secretKey()));
		this.accessTokenDecoder = decoder(TYPE_ACCESS);
		this.refreshTokenDecoder = decoder(TYPE_REFRESH);
		this.oauthStateDecoder = decoder(TYPE_OAUTH_STATE);
	}

	public String createAccessToken(Long userId, Role role) {
		return encode(String.valueOf(userId), TYPE_ACCESS, properties.accessTokenValidity(),
				builder -> builder.claim(CLAIM_ROLE, role.name()));
	}

	public String createRefreshToken(Long userId) {
		return encode(String.valueOf(userId), TYPE_REFRESH, properties.refreshTokenValidity(), builder -> {
		});
	}

	public String createOAuthState(String provider) {
		return encode("oauth", TYPE_OAUTH_STATE, OAUTH_STATE_VALIDITY,
				builder -> builder.claim(CLAIM_PROVIDER, provider));
	}

	/** refresh 토큰을 검증하고 회원 ID를 돌려준다. */
	public Long parseRefreshToken(String refreshToken) {
		try {
			return Long.valueOf(refreshTokenDecoder.decode(refreshToken).getSubject());
		} catch (JwtException | NumberFormatException e) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		}
	}

	/** state 가 이 서버가 발급했고 같은 provider 용인지 확인한다. */
	public boolean isValidOAuthState(String state, String provider) {
		if (state == null || state.isBlank()) {
			return false;
		}
		try {
			Jwt jwt = oauthStateDecoder.decode(state);
			return provider.equals(jwt.getClaimAsString(CLAIM_PROVIDER));
		} catch (JwtException e) {
			return false;
		}
	}

	/** Spring Security 리소스 서버가 쓰는 access 토큰 디코더. */
	public JwtDecoder accessTokenDecoder() {
		return accessTokenDecoder;
	}

	public long accessTokenValiditySeconds() {
		return properties.accessTokenValidity().toSeconds();
	}

	public long refreshTokenValiditySeconds() {
		return properties.refreshTokenValidity().toSeconds();
	}

	private String encode(String subject, String tokenType, Duration validity,
			java.util.function.Consumer<JwtClaimsSet.Builder> customizer) {
		Instant now = Instant.now();
		JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
				.issuer(properties.issuer())
				.subject(subject)
				.issuedAt(now)
				.expiresAt(now.plus(validity))
				.claim(CLAIM_TOKEN_TYPE, tokenType);
		customizer.accept(claims);
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
	}

	private JwtDecoder decoder(String expectedType) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(properties.secretKey())
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		OAuth2TokenValidator<Jwt> typeValidator = jwt -> expectedType.equals(jwt.getClaimAsString(CLAIM_TOKEN_TYPE))
				? OAuth2TokenValidatorResult.success()
				: OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "잘못된 토큰 종류입니다.", null));
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(properties.issuer()), typeValidator));
		return decoder;
	}
}
