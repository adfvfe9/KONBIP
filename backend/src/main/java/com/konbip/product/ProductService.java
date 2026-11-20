package com.konbip.product;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;
import com.konbip.global.response.PageResponse;
import com.konbip.product.dto.ProductResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final ProductRepository productRepository;

	/** PRODUCT-001 상품 목록 및 검색 (FR-PRD-02~04) */
	public PageResponse<ProductResponse> search(String keyword, String brand, String category, Pageable pageable) {
		return PageResponse.of(
				productRepository.search(likePattern(keyword), blankToNull(brand), blankToNull(category), pageable),
				ProductResponse::from);
	}

	/** PRODUCT-002 상품 상세 (단종 상품도 조회는 된다) */
	public ProductResponse get(Long productId) {
		return ProductResponse.from(find(productId));
	}

	public List<String> brands() {
		return productRepository.findBrands();
	}

	public List<String> categories(String brand) {
		return productRepository.findCategories(blankToNull(brand));
	}

	public Product find(Long productId) {
		return productRepository.findById(productId)
				.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
	}

	/**
	 * LIKE 검색용 패턴. %, _ 는 글자 그대로 찾도록 escape 한다.
	 * escape 문자는 '!' 이다. (쿼리에서 ESCAPE '!' 와 같이 쓴다)
	 * 백슬래시는 MySQL 문자열 리터럴에서 특수문자라 DB 마다 동작이 달라서 쓰지 않는다.
	 */
	public static String likePattern(String keyword) {
		String value = blankToNull(keyword);
		if (value == null) {
			return null;
		}
		String escaped = value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
		return "%" + escaped + "%";
	}

	public static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
