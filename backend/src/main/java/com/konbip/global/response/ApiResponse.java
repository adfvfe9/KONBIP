package com.konbip.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.konbip.global.error.ErrorCode;

/**
 * API 명세서 1.2 공통 응답 형식.
 * <pre>
 * { "success": true, "message": "...", "data": {...} }
 * { "success": false, "code": "RECIPE_NOT_FOUND", "message": "...", "data": null }
 * </pre>
 * code 는 실패 응답에만 들어간다. 프론트엔드가 메시지 대신 코드로 분기할 수 있게 추가했다.
 */
public record ApiResponse<T>(
		boolean success,
		@JsonInclude(JsonInclude.Include.NON_NULL) String code,
		String message,
		T data
) {

	private static final String DEFAULT_SUCCESS_MESSAGE = "요청이 성공적으로 처리되었습니다.";

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, null, DEFAULT_SUCCESS_MESSAGE, data);
	}

	public static <T> ApiResponse<T> ok(String message, T data) {
		return new ApiResponse<>(true, null, message, data);
	}

	public static ApiResponse<Void> ok() {
		return new ApiResponse<>(true, null, DEFAULT_SUCCESS_MESSAGE, null);
	}

	public static ApiResponse<Void> fail(ErrorCode errorCode) {
		return new ApiResponse<>(false, errorCode.name(), errorCode.getMessage(), null);
	}

	public static ApiResponse<Void> fail(ErrorCode errorCode, String message) {
		return new ApiResponse<>(false, errorCode.name(), message, null);
	}
}
