package com.konbip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;

import com.konbip.product.Product;
import com.konbip.product.ProductRepository;
import com.konbip.product.sync.ExternalProduct;
import com.konbip.product.sync.ProductSyncService;
import com.konbip.user.UserRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

	protected static final String PASSWORD = "password123";

	@Autowired
	protected MockMvc mvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected ProductSyncService productSyncService;

	@Autowired
	protected ProductRepository productRepository;

	@Autowired
	protected UserRepository userRepository;

	public record Session(Long userId, String email, String nickname, String accessToken, String refreshToken) {

		public String bearer() {
			return "Bearer " + accessToken;
		}
	}

	/** 테스트끼리 DB 를 공유하므로 이메일·닉네임·상품 ID 가 겹치지 않게 한다. */
	protected static String suffix() {
		return String.valueOf(ThreadLocalRandom.current().nextInt(100_000, 1_000_000));
	}

	protected Session signup(String nicknamePrefix) throws Exception {
		String nickname = nicknamePrefix + suffix();
		String email = "u" + suffix() + suffix() + "@test.dev";
		JsonNode body = perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(Map.of("email", email, "password", PASSWORD, "nickname", nickname))), 201);
		JsonNode data = body.get("data");
		return new Session(data.get("user").get("userId").asLong(), email, nickname,
				data.get("accessToken").asString(), data.get("refreshToken").asString());
	}

	/**
	 * 테스트 상품 4개를 넣는다.
	 * a: 가격·영양 모두 있음 / b: 가격 없음 / c: 지방 정보 없음 / d: 다른 편의점
	 */
	protected Map<String, Long> seedProducts(String brand) {
		String prefix = "t" + suffix() + "-";
		List<ExternalProduct> products = List.of(
				new ExternalProduct(prefix + "a", "테스트라면" + prefix, brand, "라면",
						new BigDecimal("300"), new BigDecimal("500"), new BigDecimal("60"), new BigDecimal("10"),
						new BigDecimal("20")),
				new ExternalProduct(prefix + "b", "테스트계란" + prefix, brand, "반찬",
						null, new BigDecimal("80"), new BigDecimal("1"), new BigDecimal("6"), new BigDecimal("5")),
				new ExternalProduct(prefix + "c", "테스트샐러드" + prefix, brand, "샐러드",
						new BigDecimal("150"), new BigDecimal("20"), new BigDecimal("4"), new BigDecimal("1"), null),
				new ExternalProduct(prefix + "d", "테스트푸딩" + prefix, "다른편의점" + prefix, "디저트",
						new BigDecimal("200"), new BigDecimal("250"), new BigDecimal("30"), new BigDecimal("4"),
						new BigDecimal("12")));
		// apply() 는 이번 목록에 없는 상품을 단종 처리하므로, 기존 상품도 함께 넘겨서 다른 테스트 데이터를 건드리지 않는다.
		List<ExternalProduct> all = new java.util.ArrayList<>(productRepository.findAll().stream()
				.map(IntegrationTestSupport::toExternal).toList());
		all.addAll(products);
		productSyncService.apply(all);

		Map<String, Long> ids = new HashMap<>();
		for (Product product : productRepository.findAll()) {
			if (product.getExternalId().startsWith(prefix)) {
				ids.put(product.getExternalId().substring(prefix.length()), product.getId());
			}
		}
		return ids;
	}

	protected static ExternalProduct toExternal(Product p) {
		return new ExternalProduct(p.getExternalId(), p.getName(), p.getBrand(), p.getCategory(), p.getPrice(),
				p.getCalories(), p.getCarbohydrate(), p.getProtein(), p.getFat());
	}

	protected JsonNode perform(AbstractMockHttpServletRequestBuilder<?> request, int expectedStatus) throws Exception {
		MvcResult result = mvc.perform(request).andReturn();
		String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		assertThat(result.getResponse().getStatus()).as(body).isEqualTo(expectedStatus);
		return body.isEmpty() ? null : objectMapper.readTree(body);
	}

	protected JsonNode perform(AbstractMockHttpServletRequestBuilder<?> request, Session session, int expectedStatus)
			throws Exception {
		return perform(request.header(HttpHeaders.AUTHORIZATION, session.bearer()), expectedStatus);
	}

	protected String json(Object value) {
		return objectMapper.writeValueAsString(value);
	}
}
