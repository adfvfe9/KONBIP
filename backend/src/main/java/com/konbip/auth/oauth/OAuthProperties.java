package com.konbip.auth.oauth;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("konbip.oauth")
public record OAuthProperties(
		String redirectBaseUri,
		String frontendRedirectUri,
		Map<String, Provider> providers
) {

	public record Provider(
			String clientId,
			String clientSecret,
			String authorizationUri,
			String tokenUri,
			String userInfoUri,
			String scope
	) {

		public boolean isConfigured() {
			return clientId != null && !clientId.isBlank();
		}
	}
}
