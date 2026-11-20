package com.konbip.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 공통
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다."),
	CONFLICT(HttpStatus.CONFLICT, "이미 처리된 요청이거나 다른 데이터와 충돌합니다."),
	PAYLOAD_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE,"업로드할 수 있는 파일 크기를 넘었습니다. (최대 5MB)"),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),

	// 인증/권한
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "인증 정보가 만료되었거나 올바르지 않습니다."),
	LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
	OAUTH_PROVIDER_NOT_SUPPORTED(HttpStatus.NOT_FOUND, "지원하지 않는 소셜 로그인입니다."),
	OAUTH_PROVIDER_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "소셜 로그인 설정이 아직 완료되지 않았습니다."),
	OAUTH_FAILED(HttpStatus.UNAUTHORIZED, "소셜 로그인에 실패했습니다."),

	// 회원
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
	DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
	EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "같은 이메일로 가입된 계정이 있습니다. 이메일로 로그인해 주세요."),
	PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다."),
	SOCIAL_ACCOUNT_NO_PASSWORD(HttpStatus.BAD_REQUEST, "소셜 로그인 회원은 비밀번호를 변경할 수 없습니다."),

	// 상품
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
	INACTIVE_PRODUCT(HttpStatus.BAD_REQUEST, "판매가 종료된 상품은 조합에 새로 담을 수 없습니다."),
	PRODUCT_SYNC_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "상품 데이터 API 설정이 아직 완료되지 않았습니다."),
	PRODUCT_SYNC_FAILED(HttpStatus.BAD_GATEWAY, "상품 데이터 동기화에 실패했습니다. 기존 데이터는 그대로 유지됩니다."),

	// 꿀조합
	RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "꿀조합을 찾을 수 없습니다."),
	NOT_RECIPE_AUTHOR(HttpStatus.FORBIDDEN, "작성자만 수정하거나 삭제할 수 있습니다."),
	TOO_MANY_OF_PRODUCT(HttpStatus.BAD_REQUEST, "한 상품은 최대 10개까지 담을 수 있습니다."),
	INVALID_IMAGE(HttpStatus.BAD_REQUEST, "jpg 또는 png 이미지(5MB 이하)만 올릴 수 있습니다."),
	IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지가 없습니다."),

	// 댓글/리뷰
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
	NOT_COMMENT_AUTHOR(HttpStatus.FORBIDDEN, "작성자만 수정하거나 삭제할 수 있습니다."),
	DUPLICATE_REVIEW(HttpStatus.CONFLICT, "이 꿀조합에는 이미 리뷰를 작성했습니다."),
	CANNOT_REVIEW_OWN_RECIPE(HttpStatus.BAD_REQUEST, "내가 등록한 꿀조합에는 리뷰를 작성할 수 없습니다."),

	// 신고
	REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다."),
	REPORT_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 대상을 찾을 수 없습니다."),
	DUPLICATE_REPORT(HttpStatus.CONFLICT, "이미 신고한 콘텐츠입니다.");

	private final HttpStatus status;
	private final String message;
}
