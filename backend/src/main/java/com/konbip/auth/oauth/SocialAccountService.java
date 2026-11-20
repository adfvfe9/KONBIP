package com.konbip.auth.oauth;

import java.security.SecureRandom;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.konbip.auth.AuthService;
import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;
import com.konbip.user.AuthProvider;
import com.konbip.user.User;
import com.konbip.user.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * FR-AUT-03 소셜 로그인: 처음 로그인하면 자동 가입시키고 임시 닉네임을 붙인다.
 * 프론트는 newUser=true 를 받으면 닉네임 설정 화면을 띄우고 PATCH /api/users/me 로 바꾸면 된다.
 */
@Service
@RequiredArgsConstructor
public class SocialAccountService {

	private static final String NICKNAME_PREFIX = "콘빕";
	private static final int NICKNAME_ATTEMPTS = 20;

	private final UserRepository userRepository;
	private final SecureRandom random = new SecureRandom();

	public record Result(User user, boolean newUser) {
	}

	@Transactional
	public Result findOrCreate(AuthProvider provider, OAuthUserInfo info) {
		Optional<User> existing = userRepository.findByProviderAndProviderUserId(provider, info.providerUserId());
		if (existing.isPresent()) {
			return new Result(existing.get(), false);
		}
		String email = AuthService.normalizeEmail(info.email());
		if (email != null && userRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED);
		}
		User user = userRepository.save(User.socialUser(provider, info.providerUserId(), email, newNickname()));
		return new Result(user, true);
	}

	private String newNickname() {
		for (int i = 0; i < NICKNAME_ATTEMPTS; i++) {
			String candidate = NICKNAME_PREFIX + String.format("%06d", random.nextInt(1_000_000));
			if (!userRepository.existsByNickname(candidate)) {
				return candidate;
			}
		}
		return NICKNAME_PREFIX + Long.toString(System.nanoTime() % 1_000_000_000_000L);
	}
}
