package com.konbip.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.") String password,
		@NotBlank @Pattern(regexp = NICKNAME_REGEX, message = NICKNAME_MESSAGE) String nickname
) {

	public static final String NICKNAME_REGEX = "^[\\p{L}\\p{N}_]{2,20}$";
	public static final String NICKNAME_MESSAGE = "닉네임은 2~20자의 한글, 영문, 숫자, _ 만 쓸 수 있습니다.";
}
