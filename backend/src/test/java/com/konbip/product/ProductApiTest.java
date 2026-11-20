package com.konbip.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.konbip.IntegrationTestSupport;
import com.konbip.product.sync.ExternalProduct;
import com.konbip.product.sync.SyncResult;

import tools.jackson.databind.JsonNode;

class ProductApiTest extends IntegrationTestSupport {

	@Test
	void searchFilterAndDetail() throws Exception {
		String brand = "상품마트" + suffix();
		Map<String, Long> p = seedProducts(brand);

		JsonNode byBrand = perform(get("/api/products").param("brand", brand), 200).get("data");
		assertThat(byBrand.get("totalElements").asLong()).isEqualTo(3);

		JsonNode byCategory = perform(get("/api/products").param("brand", brand).param("category", "반찬"), 200)
				.get("data");
		assertThat(byCategory.get("content").get(0).get("productId").asLong()).isEqualTo(p.get("b"));
		assertThat(byCategory.get("content").get(0).get("price").isNull()).isTrue();

		Product a = productRepository.findById(p.get("a")).orElseThrow();
		JsonNode byKeyword = perform(get("/api/products").param("keyword", a.getName()), 200).get("data");
		assertThat(byKeyword.get("content").get(0).get("name").asString()).isEqualTo(a.getName());

		// LIKE 와일드카드는 글자 그대로 검색
		assertThat(perform(get("/api/products").param("keyword", "%"), 200).get("data").get("totalElements").asLong())
				.isZero();

		JsonNode detail = perform(get("/api/products/" + p.get("c")), 200).get("data");
		assertThat(detail.get("fat").isNull()).isTrue();
		assertThat(detail.get("active").asBoolean()).isTrue();
		assertThat(perform(get("/api/products/999999"), 404).get("code").asString()).isEqualTo("PRODUCT_NOT_FOUND");

		List<String> brands = new ArrayList<>();
		perform(get("/api/meta/brands"), 200).get("data").forEach(node -> brands.add(node.asString()));
		assertThat(brands).contains(brand);

		List<String> categories = new ArrayList<>();
		perform(get("/api/meta/categories").param("brand", brand), 200).get("data")
				.forEach(node -> categories.add(node.asString()));
		assertThat(categories).containsExactly("라면", "반찬", "샐러드");
	}

	@Test
	void syncDeactivatesMissingProductsAndReactivatesReturningOnes() {
		String brand = "동기화마트" + suffix();
		Map<String, Long> p = seedProducts(brand);
		Product target = productRepository.findById(p.get("a")).orElseThrow();

		List<ExternalProduct> withoutTarget = productRepository.findAll().stream()
				.filter(product -> !product.getId().equals(target.getId()))
				.filter(Product::isActive)
				.map(IntegrationTestSupport::toExternal)
				.toList();
		SyncResult removed = productSyncService.apply(withoutTarget);
		assertThat(removed.deactivated()).isEqualTo(1);
		assertThat(productRepository.findById(target.getId()).orElseThrow().isActive()).isFalse();

		List<ExternalProduct> withTarget = new ArrayList<>(withoutTarget);
		withTarget.add(toExternal(target));
		SyncResult back = productSyncService.apply(withTarget);
		assertThat(back.updated()).isEqualTo(1);
		assertThat(back.created()).isZero();
		assertThat(productRepository.findById(target.getId()).orElseThrow().isActive()).isTrue();

		// 빈 응답(API 장애)은 아무것도 단종 처리하지 않는다
		assertThat(productSyncService.apply(List.of()).deactivated()).isZero();
	}
}
