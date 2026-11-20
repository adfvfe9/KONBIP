package com.konbip.auth.oauth;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.konbip.global.error.BusinessException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
public class OAuthController {

	private final OAuthService oAuthService;

	/** AUTH-005 소셜 로그인 요청. 브라우저를 이 주소로 이동시키면 된다. (provider: google, kakao) */
	@GetMapping("/{provider}")
	public ResponseEntity<Void> authorize(@PathVariable String provider) {
		URI location;
		try {
			location = oAuthService.authorizationUri(provider);
		} catch (BusinessException e) {
			location = oAuthService.errorRedirect(e.getErrorCode());
		}
		return redirect(location);
	}

	/** AUTH-006 소셜 로그인 결과 처리 (제공자 콘솔에 등록할 Redirect URI) */
	@GetMapping("/callback/{provider}")
	public ResponseEntity<Void> callback(
			@PathVariable String provider,
			@RequestParam(required = false) String code,
			@RequestParam(required = false) String state,
			@RequestParam(required = false) String error) {
		return redirect(oAuthService.handleCallback(provider, code, state, error));
	}

	private ResponseEntity<Void> redirect(URI location) {
		return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
	}
}
