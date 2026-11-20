package com.konbip.product.dto;

import java.math.BigDecimal;

import com.konbip.product.Product;

/**
 * 영양성분/가격이 API 에서 제공되지 않으면 null 이다. 프론트에서는 "미제공"으로 표시한다. (FR-PRD-05)
 */
public record ProductResponse(
		Long productId,
		String name,
		String brand,
		String category,
		BigDecimal price,
		BigDecimal calories,
		BigDecimal carbohydrate,
		BigDecimal protein,
		BigDecimal fat,
		boolean active
) {

	public static ProductResponse from(Product product) {
		return new ProductResponse(
				product.getId(),
				product.getName(),
				product.getBrand(),
				product.getCategory(),
				product.getPrice(),
				product.getCalories(),
				product.getCarbohydrate(),
				product.getProtein(),
				product.getFat(),
				product.isActive());
	}
}
