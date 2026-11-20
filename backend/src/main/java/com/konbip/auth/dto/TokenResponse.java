package com.konbip.auth.dto;

import com.konbip.user.dto.UserResponse;

/**
 * @param accessTokenExpiresIn  access 토큰 유효 시간(초)
 * @param refreshTokenExpiresIn refresh 토큰 유효 시간(초)
 */
public record TokenResponse(
		String tokenType,
		String accessToken,
		long accessTokenExpiresIn,
		String refreshToken,
		long refreshTokenExpiresIn,
		UserResponse user
) {
}
