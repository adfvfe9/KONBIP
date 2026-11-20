package com.konbip.global.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 파라미터에 로그인한 회원 ID(Long)를 넣어 준다.
 * <pre>
 * public ApiResponse<?> create(@LoginUser Long userId, ...)              // 로그인 필수
 * public ApiResponse<?> detail(@LoginUser(required = false) Long userId) // 비회원이면 null
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {

	boolean required() default true;
}
