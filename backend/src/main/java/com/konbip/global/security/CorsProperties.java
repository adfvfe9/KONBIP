package com.konbip.global.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 프론트엔드 주소 목록. 쉼표로 여러 개를 넣을 수 있고 https://*.vercel.app 같은 패턴도 된다.
 */
@ConfigurationProperties("konbip.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
