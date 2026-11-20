package com.konbip.recipe;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.konbip.recipe.dto.NutritionResponse;

/**
 * 구성 상품의 영양성분·가격 합계 계산. 없는 값은 임의로 채우지 않는다.
 */
public final class NutritionCalculator {

	private NutritionCalculator() {
	}

	public static NutritionResponse nutrition(List<RecipeItem> items) {
		List<String> missing = new ArrayList<>();
		BigDecimal calories = sum(items, RecipeItem::getCalories, "calories", missing);
		BigDecimal carbohydrate = sum(items, RecipeItem::getCarbohydrate, "carbohydrate", missing);
		BigDecimal protein = sum(items, RecipeItem::getProtein, "protein", missing);
		BigDecimal fat = sum(items, RecipeItem::getFat, "fat", missing);
		return new NutritionResponse(calories, carbohydrate, protein, fat, missing.isEmpty(), List.copyOf(missing));
	}

	/** 가격이 있는 상품만 더한 총 가격. 가격 정보가 하나도 없으면 null. (FR-CMB-07) */
	public static BigDecimal totalPrice(List<RecipeItem> items) {
		return sum(items, RecipeItem::getUnitPrice, "price", new ArrayList<>());
	}

	public static boolean isPriceComplete(List<RecipeItem> items) {
		return items.stream().allMatch(item -> item.getUnitPrice() != null);
	}

	private static BigDecimal sum(List<RecipeItem> items, Function<RecipeItem, BigDecimal> getter, String name,
			List<String> missing) {
		BigDecimal total = null;
		boolean anyMissing = false;
		for (RecipeItem item : items) {
			BigDecimal value = getter.apply(item);
			if (value == null) {
				anyMissing = true;
				continue;
			}
			BigDecimal subtotal = value.multiply(BigDecimal.valueOf(item.getQuantity()));
			total = total == null ? subtotal : total.add(subtotal);
		}
		if (anyMissing) {
			missing.add(name);
		}
		return total;
	}
}
