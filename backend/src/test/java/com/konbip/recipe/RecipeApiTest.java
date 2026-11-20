package com.konbip.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MvcResult;

import com.konbip.IntegrationTestSupport;
import com.konbip.TestImages;
import com.konbip.product.Product;

import tools.jackson.databind.JsonNode;

class RecipeApiTest extends IntegrationTestSupport {

	private static Map<String, Object> item(Long productId, int quantity) {
		Map<String, Object> item = new HashMap<>();
		item.put("productId", productId);
		item.put("quantity", quantity);
		return item;
	}

	private static Map<String, Object> item(Long productId, int quantity, int customPrice) {
		Map<String, Object> item = item(productId, quantity);
		item.put("customPrice", customPrice);
		return item;
	}

	private static Map<String, Object> recipe(String title, String brand, List<String> tags, List<String> steps,
			List<Map<String, Object>> items) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("title", title);
		body.put("description", title + " 설명");
		body.put("brand", brand);
		body.put("tags", tags);
		body.put("steps", steps);
		body.put("items", items);
		return body;
	}

	private long createJson(Session session, Map<String, Object> body) throws Exception {
		return perform(post("/api/recipes").contentType(MediaType.APPLICATION_JSON).content(json(body)), session, 201)
				.get("data").get("recipeId").asLong();
	}

	@Test
	void createWithImageAndReadDetailWithNutritionTotals() throws Exception {
		String brand = "테스트마트" + suffix();
		Map<String, Long> p = seedProducts(brand);
		Session alice = signup("앨리스");

		Map<String, Object> body = recipe("몽고탄멘 조합", brand,
				List.of("#매콤", " 반숙란 ", "매콤"),
				List.of("라면을 조리한다", "계란을 올린다"),
				List.of(item(p.get("a"), 1), item(p.get("b"), 2, 100), item(p.get("c"), 1), item(p.get("a"), 1)));

		JsonNode created = perform(multipart("/api/recipes")
				.file(new MockMultipartFile("recipe", "", "application/json", json(body).getBytes(StandardCharsets.UTF_8)))
				.file(new MockMultipartFile("image", "combo.png", "image/png", TestImages.PNG)), alice, 201);
		long recipeId = created.get("data").get("recipeId").asLong();

		JsonNode detail = perform(get("/api/recipes/" + recipeId), 200).get("data");
		assertThat(detail.get("title").asString()).isEqualTo("몽고탄멘 조합");
		assertThat(detail.get("brand").asString()).isEqualTo(brand);
		assertThat(detail.get("author").asString()).isEqualTo(alice.nickname());
		assertThat(texts(detail.get("tags"))).containsExactly("매콤", "반숙란");
		assertThat(detail.get("steps").get(0).get("order").asInt()).isEqualTo(1);
		assertThat(detail.get("steps").get(1).get("content").asString()).isEqualTo("계란을 올린다");
		assertThat(detail.get("imageUrl").asString()).isEqualTo("/api/recipes/" + recipeId + "/image");

		// a 는 두 번 보내서 수량이 2로 합쳐진다. b 는 상품 가격이 없어 직접 입력한 100원.
		JsonNode items = detail.get("items");
		assertThat(items.size()).isEqualTo(3);
		assertThat(items.get(0).get("quantity").asInt()).isEqualTo(2);
		assertThat(items.get(0).get("unitPrice").decimalValue()).isEqualByComparingTo("300");
		assertThat(items.get(1).get("unitPrice").decimalValue()).isEqualByComparingTo("100");

		// 가격: 300*2 + 100*2 + 150 = 950
		assertThat(detail.get("totalPrice").decimalValue()).isEqualByComparingTo("950");
		assertThat(detail.get("priceComplete").asBoolean()).isTrue();

		// 칼로리: 500*2 + 80*2 + 20 = 1180, 지방은 c 가 없어서 20*2 + 5*2 = 50 이고 불완전
		JsonNode nutrition = detail.get("nutrition");
		assertThat(nutrition.get("calories").decimalValue()).isEqualByComparingTo("1180");
		assertThat(detail.get("totalCalories").decimalValue()).isEqualByComparingTo("1180");
		assertThat(nutrition.get("fat").decimalValue()).isEqualByComparingTo("50");
		assertThat(nutrition.get("complete").asBoolean()).isFalse();
		assertThat(texts(nutrition.get("missingNutrients"))).containsExactly("fat");

		assertThat(detail.get("isLiked").asBoolean()).isFalse();
		assertThat(detail.get("isMine").asBoolean()).isFalse();
		assertThat(detail.get("viewCount").asLong()).isEqualTo(1);

		// 작성자 본인 조회: isMine, 로그인 회원 조회수는 하루 1번만
		JsonNode mine = perform(get("/api/recipes/" + recipeId), alice, 200).get("data");
		assertThat(mine.get("isMine").asBoolean()).isTrue();
		assertThat(mine.get("viewCount").asLong()).isEqualTo(2);
		assertThat(perform(get("/api/recipes/" + recipeId), alice, 200).get("data").get("viewCount").asLong())
				.isEqualTo(2);

		MvcResult image = mvc.perform(get("/api/recipes/" + recipeId + "/image")).andReturn();
		assertThat(image.getResponse().getStatus()).isEqualTo(200);
		assertThat(image.getResponse().getContentType()).isEqualTo("image/png");
		assertThat(image.getResponse().getContentAsByteArray()).isEqualTo(TestImages.PNG);
	}

	@Test
	void recipePartSentAsPlainFormFieldKeepsKorean() throws Exception {
		String brand = "평문마트" + suffix();
		Map<String, Long> p = seedProducts(brand);
		Session alice = signup("평문");
		String title = "한글 제목 테스트 꿀조합";
		Map<String, Object> body = recipe(title, brand, List.of("한글태그"), List.of(), List.of(item(p.get("a"), 1)));

		JsonNode created = perform(multipart("/api/recipes")
				.part(new MockPart("recipe", json(body).getBytes(StandardCharsets.UTF_8))), alice, 201);
		long recipeId = created.get("data").get("recipeId").asLong();

		JsonNode detail = perform(get("/api/recipes/" + recipeId), 200).get("data");
		assertThat(detail.get("title").asString()).isEqualTo(title);
		assertThat(detail.get("imageUrl").isNull()).isTrue();
	}

	@Test
	void rejectsInvalidRecipes() throws Exception {
		String brand = "검증마트" + suffix();
		Map<String, Long> p = seedProducts(brand);
		Session alice = signup("검증");

		JsonNode noItems = perform(post("/api/recipes").contentType(MediaType.APPLICATION_JSON)
				.content(json(recipe("상품없는조합", brand, List.of(), List.of(), List.of()))), alice, 400);
		assertThat(noItems.get("code").asString()).isEqualTo("INVALID_INPUT");

		perform(post("/api/recipes").contentType(MediaType.APPLICATION_JSON)
				.content(json(recipe("수량초과", brand, List.of(), List.of(), List.of(item(p.get("a"), 11))))), alice, 400);

		JsonNode mergedTooMany = perform(post("/api/recipes").contentType(MediaType.APPLICATION_JSON)
				.content(json(recipe("합쳐서초과", brand, List.of(), List.of(),
						List.of(item(p.get("a"), 6), item(p.get("a"), 5))))), alice, 400);
		assertThat(mergedTooMany.get("code").asString()).isEqualTo("TOO_MANY_OF_PRODUCT");

		JsonNode unknown = perform(post("/api/recipes").contentType(MediaType.APPLICATION_JSON)
				.content(json(recipe("없는상품", brand, List.of(), List.of(), List.of(item(999_999L, 1))))), alice, 404);
		assertThat(unknown.get("code").asString()).isEqualTo("PRODUCT_NOT_FOUND");

		Product discontinued = productRepository.findById(p.get("d")).orElseThrow();
		discontinued.deactivate();
		productRepository.save(discontinued);
		JsonNode inactive = perform(post("/api/recipes").contentType(MediaType.APPLICATION_JSON)
				.content(json(recipe("단종상품", brand, List.of(), List.of(), List.of(item(p.get("d"), 1))))), alice, 400);
		assertThat(inactive.get("code").asString()).isEqualTo("INACTIVE_PRODUCT");

		JsonNode badJson = perform(multipart("/api/recipes")
				.file(new MockMultipartFile("recipe", "", "application/json", "{not json".getBytes())), alice, 400);
		assertThat(badJson.get("code").asString()).isEqualTo("INVALID_INPUT");

		JsonNode badImage = perform(multipart("/api/recipes")
				.file(new MockMultipartFile("recipe", "", "application/json",
						json(recipe("이미지오류", brand, List.of(), List.of(), List.of(item(p.get("a"), 1))))
								.getBytes(StandardCharsets.UTF_8)))
				.file(new MockMultipartFile("image", "x.gif", "image/gif", "GIF89a".getBytes())), alice, 400);
		assertThat(badImage.get("code").asString()).isEqualTo("INVALID_IMAGE");

		JsonNode anonymous = perform(post("/api/recipes").contentType(MediaType.APPLICATION_JSON)
				.content(json(recipe("비회원", brand, List.of(), List.of(), List.of(item(p.get("a"), 1))))), 401);
		assertThat(anonymous.get("code").asString()).isEqualTo("UNAUTHORIZED");
	}

	@Test
	void onlyAuthorCanUpdateAndDelete() throws Exception {
		String brand = "수정마트" + suffix();
		Map<String, Long> p = seedProducts(brand);
		Session alice = signup("작성자");
		Session bob = signup("남");

		long recipeId = createJson(alice, recipe("수정 전", brand, List.of("x", "y"), List.of("s1", "s2", "s3"),
				List.of(item(p.get("a"), 1), item(p.get("b"), 1, 50))));

		Map<String, Object> changed = recipe("수정 후", brand, List.of("y", "z"), List.of("n1"),
				List.of(item(p.get("a"), 3), item(p.get("c"), 1)));

		JsonNode forbidden = perform(patch("/api/recipes/" + recipeId).contentType(MediaType.APPLICATION_JSON)
				.content(json(changed)), bob, 403);
		assertThat(forbidden.get("code").asString()).isEqualTo("NOT_RECIPE_AUTHOR");

		perform(patch("/api/recipes/" + recipeId).contentType(MediaType.APPLICATION_JSON).content(json(changed)),
				alice, 200);
		JsonNode detail = perform(get("/api/recipes/" + recipeId), 200).get("data");
		assertThat(detail.get("title").asString()).isEqualTo("수정 후");
		assertThat(texts(detail.get("tags"))).containsExactly("y", "z");
		assertThat(detail.get("steps").size()).isEqualTo(1);
		Map<Long, Integer> quantities = new HashMap<>();
		detail.get("items").forEach(i -> quantities.put(i.get("productId").asLong(), i.get("quantity").asInt()));
		assertThat(quantities).containsExactlyInAnyOrderEntriesOf(Map.of(p.get("a"), 3, p.get("c"), 1));

		// multipart 수정: 이미지 추가 → removeImage 로 삭제
		perform(multipart(HttpMethod.PATCH, "/api/recipes/" + recipeId)
				.file(new MockMultipartFile("recipe", "", "application/json", json(changed).getBytes(StandardCharsets.UTF_8)))
				.file(new MockMultipartFile("image", "new.png", "image/png", TestImages.PNG)), alice, 200);
		assertThat(perform(get("/api/recipes/" + recipeId), 200).get("data").get("imageUrl").isNull()).isFalse();

		perform(multipart(HttpMethod.PATCH, "/api/recipes/" + recipeId)
				.file(new MockMultipartFile("recipe", "", "application/json", json(changed).getBytes(StandardCharsets.UTF_8)))
				.param("removeImage", "true"), alice, 200);
		assertThat(perform(get("/api/recipes/" + recipeId), 200).get("data").get("imageUrl").isNull()).isTrue();

		perform(delete("/api/recipes/" + recipeId), bob, 403);
		perform(delete("/api/recipes/" + recipeId), alice, 200);
		JsonNode gone = perform(get("/api/recipes/" + recipeId), 404);
		assertThat(gone.get("code").asString()).isEqualTo("RECIPE_NOT_FOUND");
	}

	@Test
	void searchFilterAndSort() throws Exception {
		String brand = "검색마트" + suffix();
		Map<String, Long> p = seedProducts(brand);
		Session alice = signup("검색");
		Session bob = signup("좋아요");
		String uniqueTag = "태그" + suffix();

		long ramen = createJson(alice, recipe("라면 조합", brand, List.of(uniqueTag), List.of(),
				List.of(item(p.get("a"), 1))));
		long pudding = createJson(alice, recipe("푸딩 조합", null, List.of(uniqueTag, "디저트"), List.of(),
				List.of(item(p.get("d"), 1))));
		perform(post("/api/recipes/" + pudding + "/like"), bob, 200);

		assertThat(recipeIds(perform(get("/api/recipes").param("keyword", uniqueTag), 200)))
				.containsExactly(pudding, ramen);
		assertThat(recipeIds(perform(get("/api/recipes").param("keyword", uniqueTag).param("sort", "popular"), 200)))
				.startsWith(pudding);
		assertThat(recipeIds(perform(get("/api/recipes").param("tag", "#" + uniqueTag), 200)))
				.containsExactlyInAnyOrder(ramen, pudding);
		// 상품명으로 검색
		Product a = productRepository.findById(p.get("a")).orElseThrow();
		assertThat(recipeIds(perform(get("/api/recipes").param("keyword", a.getName()), 200))).containsExactly(ramen);
		// 대표 편의점 또는 구성 상품의 편의점
		assertThat(recipeIds(perform(get("/api/recipes").param("brand", brand), 200))).containsExactly(ramen);
		assertThat(recipeIds(perform(get("/api/recipes").param("keyword", uniqueTag).param("category", "디저트"), 200)))
				.containsExactly(pudding);

		JsonNode page = perform(get("/api/recipes").param("keyword", uniqueTag).param("size", "1"), 200).get("data");
		assertThat(page.get("content").size()).isEqualTo(1);
		assertThat(page.get("totalElements").asLong()).isEqualTo(2);
		assertThat(page.get("totalPages").asInt()).isEqualTo(2);

		JsonNode card = perform(get("/api/recipes").param("keyword", uniqueTag), 200).get("data").get("content").get(0);
		assertThat(card.get("likeCount").asLong()).isEqualTo(1);
		assertThat(texts(card.get("tags"))).containsExactly(uniqueTag, "디저트");
		assertThat(card.get("totalCalories").decimalValue()).isEqualByComparingTo("250");

		JsonNode badSort = perform(get("/api/recipes").param("sort", "weird"), 400);
		assertThat(badSort.get("code").asString()).isEqualTo("INVALID_INPUT");
	}

	private static List<Long> recipeIds(JsonNode body) {
		List<Long> ids = new ArrayList<>();
		body.get("data").get("content").forEach(card -> ids.add(card.get("recipeId").asLong()));
		return ids;
	}

	private static List<String> texts(JsonNode array) {
		List<String> values = new ArrayList<>();
		array.forEach(node -> values.add(node.asString()));
		return values;
	}
}
