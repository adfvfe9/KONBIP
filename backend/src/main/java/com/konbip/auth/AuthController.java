package com.konbip.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.konbip.auth.dto.LoginRequest;
import com.konbip.auth.dto.RefreshRequest;
import com.konbip.auth.dto.SignupRequest;
import com.konbip.auth.dto.TokenResponse;
import com.konbip.global.response.ApiResponse;
import com.konbip.global.security.LoginUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	/** AUTH-001 회원가입 */
	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<TokenResponse> signup(@Valid @RequestBody SignupRequest request) {
		return ApiResponse.ok("회원가입이 완료되었습니다.", authService.signup(request));
	}

	/** AUTH-002 로그인 */
	@PostMapping("/login")
	public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
		return ApiResponse.ok("로그인되었습니다.", authService.login(request));
	}

	/**
	 * AUTH-003 로그아웃.
	 * JWT 는 서버에 저장하지 않으므로(stateless) 클라이언트가 보관한 토큰을 지우면 로그아웃된다.
	 */
	@PostMapping("/logout")
	public ApiResponse<Void> logout(@LoginUser Long userId) {
		return ApiResponse.ok("로그아웃되었습니다.", null);
	}

	/** AUTH-004 토큰 갱신 */
	@PostMapping("/refresh")
	public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
		return ApiResponse.ok(authService.refresh(request.refreshToken()));
	}
}
