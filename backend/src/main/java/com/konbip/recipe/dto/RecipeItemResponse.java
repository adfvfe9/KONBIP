package com.konbip.recipe.dto;

import java.math.BigDecimal;

import com.konbip.product.Product;
import com.konbip.recipe.RecipeItem;

/**
 * 꿀조합 구성 상품. 가격·영양성분은 상품 1개 기준이다. (합계 = 값 × quantity)
 *
 * @param active false 면 단종된 상품
 */
public record RecipeItemResponse(
		Long productId,
		String name,
		String brand,
		String category,
		int quantity,
		BigDecimal unitPrice,
		BigDecimal calories,
		BigDecimal carbohydrate,
		BigDecimal protein,
		BigDecimal fat,
		boolean active
) {

	public static RecipeItemResponse from(RecipeItem item) {
		Product product = item.getProduct();
		return new RecipeItemResponse(
				product.getId(),
				product.getName(),
				product.getBrand(),
				product.getCategory(),
				item.getQuantity(),
				item.getUnitPrice(),
				item.getCalories(),
				item.getCarbohydrate(),
				item.getProtein(),
				item.getFat(),
				product.isActive());
	}
}
