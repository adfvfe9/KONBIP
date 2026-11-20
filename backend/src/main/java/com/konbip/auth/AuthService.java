package com.konbip.auth;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.konbip.auth.dto.LoginRequest;
import com.konbip.auth.dto.SignupRequest;
import com.konbip.auth.dto.TokenResponse;
import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;
import com.konbip.global.security.JwtTokenProvider;
import com.konbip.user.User;
import com.konbip.user.UserRepository;
import com.konbip.user.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	/** FR-AUT-01 이메일 회원가입. 가입하면 바로 로그인된 상태(토큰 발급)로 돌려준다. */
	@Transactional
	public TokenResponse signup(SignupRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}
		if (userRepository.existsByNickname(request.nickname())) {
			throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
		}
		User user = userRepository.save(
				User.emailUser(email, passwordEncoder.encode(request.password()), request.nickname()));
		return issueTokens(user);
	}

	/** FR-AUT-02 이메일 로그인. 어느 항목이 틀렸는지 알려주지 않는다. */
	public TokenResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(normalizeEmail(request.email()))
				.filter(found -> found.getPasswordHash() != null)
				.filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
				.orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
		return issueTokens(user);
	}

	/** AUTH-004 refresh 토큰으로 새 access/refresh 토큰을 발급한다. */
	public TokenResponse refresh(String refreshToken) {
		Long userId = jwtTokenProvider.parseRefreshToken(refreshToken);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
		return issueTokens(user);
	}

	public TokenResponse issueTokens(User user) {
		return new TokenResponse(
				"Bearer",
				jwtTokenProvider.createAccessToken(user.getId(), user.getRole()),
				jwtTokenProvider.accessTokenValiditySeconds(),
				jwtTokenProvider.createRefreshToken(user.getId()),
				jwtTokenProvider.refreshTokenValiditySeconds(),
				UserResponse.from(user));
	}

	public static String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}
}
