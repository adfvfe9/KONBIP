package com.konbip.user;

import java.util.Locale;

import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;

/**
 * 소셜 로그인 제공자. 이메일 가입 회원은 users.provider 가 NULL 이다.
 */
public enum AuthProvider {
	GOOGLE,
	KAKAO;

	public static AuthProvider from(String value) {
		for (AuthProvider provider : values()) {
			if (provider.name().equalsIgnoreCase(value)) {
				return provider;
			}
		}
		throw new BusinessException(ErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED);
	}

	/** 설정 키, URL 경로에 쓰는 소문자 이름 (google, kakao) */
	public String key() {
		return name().toLowerCase(Locale.ROOT);
	}
}
