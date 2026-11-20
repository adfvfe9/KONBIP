package com.konbip.user.dto;

import com.konbip.auth.dto.SignupRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequest(
		@NotBlank
		@Pattern(regexp = SignupRequest.NICKNAME_REGEX, message = SignupRequest.NICKNAME_MESSAGE)
		String nickname
) {
}
