package com.konbip.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import com.konbip.IntegrationTestSupport;
import com.konbip.TestImages;

import tools.jackson.databind.JsonNode;

class AuthApiTest extends IntegrationTestSupport {

	@Test
	void signupLoginAndMe() throws Exception {
		Session alice = signup("앨리스");

		JsonNode me = perform(get("/api/users/me"), alice, 200).get("data");
		assertThat(me.get("nickname").asString()).isEqualTo(alice.nickname());
		assertThat(me.get("provider").asString()).isEqualTo("EMAIL");
		assertThat(me.get("role").asString()).isEqualTo("USER");

		JsonNode duplicateEmail = perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("email", alice.email(), "password", PASSWORD, "nickname", "다른닉" + suffix()))), 409);
		assertThat(duplicateEmail.get("success").asBoolean()).isFalse();
		assertThat(duplicateEmail.get("code").asString()).isEqualTo("DUPLICATE_EMAIL");
		assertThat(duplicateEmail.get("data").isNull()).isTrue();

		JsonNode duplicateNickname = perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("email", "x" + suffix() + "@test.dev", "password", PASSWORD,
						"nickname", alice.nickname()))), 409);
		assertThat(duplicateNickname.get("code").asString()).isEqualTo("DUPLICATE_NICKNAME");

		JsonNode shortPassword = perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("email", "y" + suffix() + "@test.dev", "password", "short",
						"nickname", "닉" + suffix()))), 400);
		assertThat(shortPassword.get("code").asString()).isEqualTo("INVALID_INPUT");

		JsonNode wrongPassword = perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("email", alice.email(), "password", "wrong-password"))), 401);
		assertThat(wrongPassword.get("code").asString()).isEqualTo("LOGIN_FAILED");

		JsonNode login = perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("email", alice.email().toUpperCase(), "password", PASSWORD))), 200);
		assertThat(login.get("data").get("tokenType").asString()).isEqualTo("Bearer");
		assertThat(login.get("data").get("accessToken").asString()).isNotBlank();
	}

	@Test
	void protectedApiRequiresValidAccessToken() throws Exception {
		JsonNode noToken = perform(get("/api/users/me"), 401);
		assertThat(noToken.get("code").asString()).isEqualTo("UNAUTHORIZED");

		JsonNode badToken = perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"), 401);
		assertThat(badToken.get("code").asString()).isEqualTo("INVALID_TOKEN");

		// 공개 API 는 토큰 없이 된다
		perform(get("/api/home"), 200);
		perform(get("/api/recipes"), 200);
		perform(get("/api/products"), 200);
	}

	@Test
	void refreshTokenIssuesNewTokensAndCannotBeUsedAsAccessToken() throws Exception {
		Session bob = signup("밥");

		JsonNode refreshed = perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("refreshToken", bob.refreshToken()))), 200).get("data");
		String newAccess = refreshed.get("accessToken").asString();
		perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccess), 200);

		JsonNode refreshAsAccess = perform(get("/api/users/me")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + bob.refreshToken()), 401);
		assertThat(refreshAsAccess.get("code").asString()).isEqualTo("INVALID_TOKEN");

		JsonNode accessAsRefresh = perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("refreshToken", bob.accessToken()))), 401);
		assertThat(accessAsRefresh.get("code").asString()).isEqualTo("INVALID_TOKEN");

		perform(post("/api/auth/logout"), bob, 200);
	}

	@Test
	void updateNicknameAndPassword() throws Exception {
		Session carol = signup("캐롤");
		Session other = signup("다른이");

		String newNickname = "새닉네임" + suffix();
		JsonNode updated = perform(patch("/api/users/me").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("nickname", newNickname))), carol, 200);
		assertThat(updated.get("data").get("nickname").asString()).isEqualTo(newNickname);

		JsonNode taken = perform(patch("/api/users/me").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("nickname", other.nickname()))), carol, 409);
		assertThat(taken.get("code").asString()).isEqualTo("DUPLICATE_NICKNAME");

		JsonNode invalid = perform(patch("/api/users/me").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("nickname", "공 백"))), carol, 400);
		assertThat(invalid.get("code").asString()).isEqualTo("INVALID_INPUT");

		JsonNode wrongCurrent = perform(patch("/api/users/me/password").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("currentPassword", "nope-nope", "newPassword", "newpassword1"))), carol, 400);
		assertThat(wrongCurrent.get("code").asString()).isEqualTo("PASSWORD_MISMATCH");

		perform(patch("/api/users/me/password").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("currentPassword", PASSWORD, "newPassword", "newpassword1"))), carol, 200);
		perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("email", carol.email(), "password", "newpassword1"))), 200);
		perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("email", carol.email(), "password", PASSWORD))), 401);
	}

	@Test
	void profileImageUploadReadDelete() throws Exception {
		Session dave = signup("데이브");

		JsonNode uploaded = perform(multipart(HttpMethod.PUT, "/api/users/me/profile-image")
				.file(new MockMultipartFile("image", "me.png", "image/png", TestImages.PNG)), dave, 200);
		String url = uploaded.get("data").get("profileImageUrl").asString();
		assertThat(url).isEqualTo("/api/users/" + dave.userId() + "/profile-image");

		MvcResult image = mvc.perform(get(url)).andReturn();
		assertThat(image.getResponse().getStatus()).isEqualTo(200);
		assertThat(image.getResponse().getContentType()).isEqualTo("image/png");
		assertThat(image.getResponse().getContentAsByteArray()).isEqualTo(TestImages.PNG);

		JsonNode notImage = perform(multipart(HttpMethod.PUT, "/api/users/me/profile-image")
				.file(new MockMultipartFile("image", "evil.png", "image/png", "<html>".getBytes())), dave, 400);
		assertThat(notImage.get("code").asString()).isEqualTo("INVALID_IMAGE");

		JsonNode deleted = perform(delete("/api/users/me/profile-image"), dave, 200);
		assertThat(deleted.get("data").get("profileImageUrl").isNull()).isTrue();
		perform(get(url), 404);
	}

	@Test
	void socialLoginRedirectsToFrontendWithErrorWhenNotConfigured() throws Exception {
		MvcResult google = mvc.perform(get("/api/auth/oauth2/google")).andReturn();
		assertThat(google.getResponse().getStatus()).isEqualTo(302);
		assertThat(google.getResponse().getHeader(HttpHeaders.LOCATION))
				.isEqualTo("http://localhost:5173/oauth/callback#error=OAUTH_PROVIDER_NOT_CONFIGURED");

		MvcResult unknown = mvc.perform(get("/api/auth/oauth2/naver")).andReturn();
		assertThat(unknown.getResponse().getHeader(HttpHeaders.LOCATION))
				.endsWith("#error=OAUTH_PROVIDER_NOT_SUPPORTED");

		MvcResult forgedCallback = mvc.perform(get("/api/auth/oauth2/callback/kakao")
				.param("code", "abc").param("state", "forged")).andReturn();
		assertThat(forgedCallback.getResponse().getHeader(HttpHeaders.LOCATION)).endsWith("#error=OAUTH_FAILED");
	}
}
