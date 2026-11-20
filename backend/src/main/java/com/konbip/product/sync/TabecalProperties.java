package com.konbip.product.sync;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * API Key 는 코드에 넣지 않고 환경변수(TABECAL_API_KEY)로만 받는다. (NFR-SEC-05)
 */
@ConfigurationProperties("konbip.tabecal")
public record TabecalProperties(String baseUrl, String apiKey) {

	public boolean isConfigured() {
		return baseUrl != null && !baseUrl.isBlank() && apiKey != null && !apiKey.isBlank();
	}
}
