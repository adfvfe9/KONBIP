package com.konbip.interaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.konbip.IntegrationTestSupport;
import com.konbip.user.Role;
import com.konbip.user.User;

import tools.jackson.databind.JsonNode;

/**
 * 좋아요·즐겨찾기·댓글·리뷰·랭킹·마이페이지·신고·관리자·회원 탈퇴
 */
class SocialApiTest extends IntegrationTestSupport {

	private long createRecipe(Session author, Long productId, String title) throws Exception {
		Map<String, Object> body = Map.of(
				"title", title,
				"tags", List.of("소셜"),
				"items", List.of(Map.of("productId", productId, "quantity", 1)));
		return perform(post("/api/recipes").contentType(MediaType.APPLICATION_JSON).content(json(body)), author, 201)
				.get("data").get("recipeId").asLong();
	}

	private JsonNode review(Session session, long recipeId, int rating, int expectedStatus) throws Exception {
		return perform(post("/api/recipes/" + recipeId + "/reviews").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("rating", rating, "content", "리뷰 " + rating))), session, expectedStatus);
	}

	@Test
	void likeAndBookmarkAreIdempotentAndShowInMyPage() throws Exception {
		Long productId = seedProducts("좋아요마트" + suffix()).get("a");
		Session author = signup("작가");
		Session fan = signup("팬");
		long recipeId = createRecipe(author, productId, "좋아요 받을 조합");

		JsonNode liked = perform(post("/api/recipes/" + recipeId + "/like"), fan, 200).get("data");
		assertThat(liked.get("isLiked").asBoolean()).isTrue();
		assertThat(liked.get("likeCount").asLong()).isEqualTo(1);
		assertThat(perform(post("/api/recipes/" + recipeId + "/like"), fan, 200).get("data").get("likeCount").asLong())
				.isEqualTo(1);

		JsonNode bookmarked = perform(post("/api/recipes/" + recipeId + "/bookmark"), fan, 200).get("data");
		assertThat(bookmarked.get("isBookmarked").asBoolean()).isTrue();
		assertThat(bookmarked.get("bookmarkCount").asLong()).isEqualTo(1);

		JsonNode detail = perform(get("/api/recipes/" + recipeId), fan, 200).get("data");
		assertThat(detail.get("isLiked").asBoolean()).isTrue();
		assertThat(detail.get("isBookmarked").asBoolean()).isTrue();

		assertThat(ids(perform(get("/api/users/me/likes"), fan, 200))).containsExactly(recipeId);
		assertThat(ids(perform(get("/api/users/me/bookmarks"), fan, 200))).containsExactly(recipeId);
		assertThat(ids(perform(get("/api/users/me/recipes"), author, 200))).containsExactly(recipeId);

		JsonNode unliked = perform(delete("/api/recipes/" + recipeId + "/like"), fan, 200).get("data");
		assertThat(unliked.get("isLiked").asBoolean()).isFalse();
		assertThat(unliked.get("likeCount").asLong()).isZero();
		perform(delete("/api/recipes/" + recipeId + "/like"), fan, 200);
		perform(delete("/api/recipes/" + recipeId + "/bookmark"), fan, 200);
		assertThat(ids(perform(get("/api/users/me/likes"), fan, 200))).isEmpty();

		perform(post("/api/recipes/999999/like"), fan, 404);
		perform(post("/api/recipes/" + recipeId + "/like"), 401);
	}

	@Test
	void reviewAndCommentRules() throws Exception {
		Long productId = seedProducts("리뷰마트" + suffix()).get("a");
		Session author = signup("요리사");
		Session guest = signup("손님");
		long recipeId = createRecipe(author, productId, "리뷰 받을 조합");

		assertThat(review(author, recipeId, 5, 400).get("code").asString()).isEqualTo("CANNOT_REVIEW_OWN_RECIPE");
		long reviewId = review(guest, recipeId, 4, 201).get("data").get("reviewId").asLong();
		assertThat(review(guest, recipeId, 5, 409).get("code").asString()).isEqualTo("DUPLICATE_REVIEW");
		perform(post("/api/recipes/" + recipeId + "/reviews").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("rating", 6))), guest, 400);

		JsonNode detail = perform(get("/api/recipes/" + recipeId), guest, 200).get("data");
		assertThat(detail.get("averageRating").asDouble()).isEqualTo(4.0);
		assertThat(detail.get("reviewCount").asLong()).isEqualTo(1);
		assertThat(detail.get("isReviewed").asBoolean()).isTrue();

		perform(patch("/api/reviews/" + reviewId).contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("rating", 2, "content", "생각보다 별로"))), author, 403);
		JsonNode edited = perform(patch("/api/reviews/" + reviewId).contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("rating", 2, "content", "생각보다 별로"))), guest, 200).get("data");
		assertThat(edited.get("rating").asInt()).isEqualTo(2);

		JsonNode myReviews = perform(get("/api/users/me/reviews"), guest, 200).get("data").get("content");
		assertThat(myReviews.get(0).get("recipeTitle").asString()).isEqualTo("리뷰 받을 조합");

		long commentId = perform(post("/api/recipes/" + recipeId + "/comments").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("content", "맛있어 보여요!"))), guest, 201).get("data").get("commentId").asLong();
		perform(post("/api/recipes/" + recipeId + "/comments").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("content", "  "))), guest, 400);
		perform(post("/api/recipes/" + recipeId + "/comments").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("content", "가".repeat(301)))), guest, 400);
		perform(patch("/api/comments/" + commentId).contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("content", "남의 댓글 수정"))), author, 403);
		// 리뷰 ID 로 댓글 API 를 부르면 없는 댓글로 처리
		perform(patch("/api/comments/" + reviewId).contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("content", "x"))), guest, 404);

		JsonNode comments = perform(get("/api/recipes/" + recipeId + "/comments"), 200).get("data");
		assertThat(comments.get("totalElements").asLong()).isEqualTo(1);
		assertThat(comments.get("content").get(0).get("author").asString()).isEqualTo(guest.nickname());
		JsonNode reviews = perform(get("/api/recipes/" + recipeId + "/reviews"), 200).get("data");
		assertThat(reviews.get("totalElements").asLong()).isEqualTo(1);

		perform(delete("/api/comments/" + commentId), guest, 200);
		perform(delete("/api/reviews/" + reviewId), guest, 200);
		assertThat(perform(get("/api/recipes/" + recipeId), 200).get("data").get("reviewCount").asLong()).isZero();
	}

	@Test
	void rankingAndHome() throws Exception {
		Long productId = seedProducts("랭킹마트" + suffix()).get("a");
		Session author = signup("랭커");
		long recipeId = createRecipe(author, productId, "랭킹 조합");

		List<Session> reviewers = List.of(signup("평가1"), signup("평가2"));
		for (Session reviewer : reviewers) {
			review(reviewer, recipeId, 5, 201);
			perform(get("/api/recipes/" + recipeId), reviewer, 200);
			perform(post("/api/recipes/" + recipeId + "/bookmark"), reviewer, 200);
		}

		assertThat(rankedIds(perform(get("/api/rankings").param("type", "views").param("period", "daily")
				.param("size", "100"), 200))).contains(recipeId);
		assertThat(rankedIds(perform(get("/api/rankings").param("type", "bookmarks").param("period", "weekly")
				.param("size", "100"), 200))).contains(recipeId);
		// 리뷰 2개는 별점 랭킹 기준(3개)에 못 미친다
		assertThat(rankedIds(perform(get("/api/rankings").param("type", "rating").param("period", "all")
				.param("size", "100"), 200))).doesNotContain(recipeId);

		review(signup("평가3"), recipeId, 4, 201);
		JsonNode rating = perform(get("/api/rankings").param("type", "rating").param("period", "monthly")
				.param("size", "100"), 200).get("data");
		JsonNode entry = null;
		for (JsonNode item : rating.get("content")) {
			if (item.get("recipe").get("recipeId").asLong() == recipeId) {
				entry = item;
			}
		}
		assertThat(entry).isNotNull();
		assertThat(entry.get("score").asDouble()).isEqualTo(4.67);
		assertThat(entry.get("rank").asInt()).isPositive();

		perform(get("/api/rankings").param("period", "yearly"), 400);

		JsonNode home = perform(get("/api/home"), 200).get("data");
		assertThat(home.get("latestRecipes").isArray()).isTrue();
		assertThat(home.get("popularRecipes").isArray()).isTrue();
	}

	@Test
	void reportsAndAdminApis() throws Exception {
		Long productId = seedProducts("신고마트" + suffix()).get("a");
		Session author = signup("피신고");
		Session reporter = signup("신고자");
		long recipeId = createRecipe(author, productId, "신고될 조합");

		Map<String, Object> report = Map.of("targetType", "RECIPE", "targetId", recipeId, "reason", "광고");
		long reportId = perform(post("/api/reports").contentType(MediaType.APPLICATION_JSON).content(json(report)),
				reporter, 201).get("data").get("reportId").asLong();
		assertThat(perform(post("/api/reports").contentType(MediaType.APPLICATION_JSON).content(json(report)),
				reporter, 409).get("code").asString()).isEqualTo("DUPLICATE_REPORT");
		perform(post("/api/reports").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("targetType", "COMMENT", "targetId", 987654, "reason", "욕설"))), reporter, 404);

		// 일반 회원은 관리자 API 불가
		assertThat(perform(get("/api/admin/reports"), reporter, 403).get("code").asString()).isEqualTo("FORBIDDEN");

		Session admin = signup("관리");
		User adminUser = userRepository.findById(admin.userId()).orElseThrow();
		adminUser.grantRole(Role.ADMIN);
		userRepository.save(adminUser);
		JsonNode login = perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("email", admin.email(), "password", PASSWORD))), 200).get("data");
		Session adminSession = new Session(admin.userId(), admin.email(), admin.nickname(),
				login.get("accessToken").asString(), login.get("refreshToken").asString());

		JsonNode pending = perform(get("/api/admin/reports").param("status", "pending").param("size", "100"),
				adminSession, 200).get("data");
		assertThat(pending.get("totalElements").asLong()).isPositive();

		JsonNode handled = perform(patch("/api/admin/reports/" + reportId).contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("status", "RESOLVED"))), adminSession, 200).get("data");
		assertThat(handled.get("status").asString()).isEqualTo("RESOLVED");

		perform(delete("/api/admin/recipes/" + recipeId), adminSession, 200);
		perform(get("/api/recipes/" + recipeId), 404);

		JsonNode sync = perform(post("/api/admin/products/sync"), adminSession, 503);
		assertThat(sync.get("code").asString()).isEqualTo("PRODUCT_SYNC_NOT_CONFIGURED");
	}

	@Test
	void withdrawDeletesUserAndTheirContent() throws Exception {
		Long productId = seedProducts("탈퇴마트" + suffix()).get("a");
		Session leaving = signup("떠남");
		Session staying = signup("남음");
		long leavingRecipe = createRecipe(leaving, productId, "탈퇴자 조합");
		long stayingRecipe = createRecipe(staying, productId, "남는 조합");

		perform(post("/api/recipes/" + stayingRecipe + "/like"), leaving, 200);
		review(leaving, stayingRecipe, 5, 201);
		perform(post("/api/recipes/" + leavingRecipe + "/like"), staying, 200);
		perform(post("/api/reports").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("targetType", "RECIPE", "targetId", stayingRecipe, "reason", "테스트"))), leaving, 201);

		perform(delete("/api/users/me"), leaving, 200);

		perform(get("/api/recipes/" + leavingRecipe), 404);
		JsonNode remaining = perform(get("/api/recipes/" + stayingRecipe), 200).get("data");
		assertThat(remaining.get("likeCount").asLong()).isZero();
		assertThat(remaining.get("reviewCount").asLong()).isZero();
		assertThat(ids(perform(get("/api/users/me/likes"), staying, 200))).doesNotContain(leavingRecipe);

		perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("email", leaving.email(), "password", PASSWORD))), 401);
		assertThat(userRepository.findById(leaving.userId())).isEmpty();
	}

	private static List<Long> ids(JsonNode body) {
		List<Long> ids = new ArrayList<>();
		body.get("data").get("content").forEach(card -> ids.add(card.get("recipeId").asLong()));
		return ids;
	}

	private static List<Long> rankedIds(JsonNode body) {
		List<Long> ids = new ArrayList<>();
		body.get("data").get("content").forEach(item -> ids.add(item.get("recipe").get("recipeId").asLong()));
		return ids;
	}
}
