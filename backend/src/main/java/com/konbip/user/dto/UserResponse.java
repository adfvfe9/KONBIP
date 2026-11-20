package com.konbip.user.dto;

import java.time.LocalDateTime;

import com.konbip.user.User;

/**
 * @param provider EMAIL, GOOGLE, KAKAO 중 하나. EMAIL 이 아니면 비밀번호 변경 메뉴를 숨긴다.
 */
public record UserResponse(
		Long userId,
		String email,
		String nickname,
		String role,
		String provider,
		String profileImageUrl,
		LocalDateTime createdAt
) {

	public static UserResponse from(User user) {
		return from(user, user.hasProfileImage());
	}

	public static UserResponse from(User user, boolean hasProfileImage) {
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getNickname(),
				user.getRole().name(),
				user.getProvider() == null ? "EMAIL" : user.getProvider().name(),
				hasProfileImage ? profileImageUrl(user.getId()) : null,
				user.getCreatedAt());
	}

	public static String profileImageUrl(Long userId) {
		return "/api/users/" + userId + "/profile-image";
	}
}
