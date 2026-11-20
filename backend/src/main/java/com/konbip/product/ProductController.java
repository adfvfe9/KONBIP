package com.konbip.product;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.konbip.global.response.ApiResponse;
import com.konbip.global.response.PageResponse;
import com.konbip.global.response.Pages;
import com.konbip.product.dto.ProductResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	/** PRODUCT-001 상품 목록 및 검색 */
	@GetMapping("/api/products")
	public ApiResponse<PageResponse<ProductResponse>> search(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String brand,
			@RequestParam(required = false) String category,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ApiResponse.ok("상품 조회 성공",
				productService.search(keyword, brand, category, Pages.of(page, size, Sort.by("name", "id"))));
	}

	/** PRODUCT-002 상품 상세 */
	@GetMapping("/api/products/{productId}")
	public ApiResponse<ProductResponse> get(@PathVariable Long productId) {
		return ApiResponse.ok("상품 조회 성공", productService.get(productId));
	}

	/** PRODUCT-003 브랜드(편의점) 목록 */
	@GetMapping("/api/meta/brands")
	public ApiResponse<List<String>> brands() {
		return ApiResponse.ok(productService.brands());
	}

	/** PRODUCT-004 카테고리 목록. brand 를 주면 그 편의점의 카테고리만 */
	@GetMapping("/api/meta/categories")
	public ApiResponse<List<String>> categories(@RequestParam(required = false) String brand) {
		return ApiResponse.ok(productService.categories(brand));
	}
}
