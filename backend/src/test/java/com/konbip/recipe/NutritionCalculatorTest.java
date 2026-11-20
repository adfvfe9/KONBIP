package com.konbip.recipe;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.konbip.product.Product;
import com.konbip.product.sync.ExternalProduct;
import com.konbip.recipe.dto.NutritionResponse;
import com.konbip.user.User;

class NutritionCalculatorTest {

	private final Recipe recipe = Recipe.create(User.emailUser("a@test.dev", "hash", "테스트"), "조합", null, null);

	private static Product product(String price, String calories, String carbohydrate, String protein, String fat) {
		return Product.from(new ExternalProduct("id", "상품", "브랜드", "카테고리", dec(price), dec(calories),
				dec(carbohydrate), dec(protein), dec(fat)));
	}

	private static BigDecimal dec(String value) {
		return value == null ? null : new BigDecimal(value);
	}

	private RecipeItem item(Product product, int quantity, String unitPrice) {
		return RecipeItem.of(recipe, new RecipeItem.Draft(product, quantity, dec(unitPrice)));
	}

	@Test
	void sumsValuesTimesQuantity() {
		List<RecipeItem> items = List.of(
				item(product("100", "200", "30", "5", "7.5"), 2, "100"),
				item(product("50", "10", "1", "0.5", "0"), 3, "50"));

		NutritionResponse nutrition = NutritionCalculator.nutrition(items);

		assertThat(nutrition.calories()).isEqualByComparingTo("430");
		assertThat(nutrition.carbohydrate()).isEqualByComparingTo("63");
		assertThat(nutrition.protein()).isEqualByComparingTo("11.5");
		assertThat(nutrition.fat()).isEqualByComparingTo("15");
		assertThat(nutrition.complete()).isTrue();
		assertThat(nutrition.missingNutrients()).isEmpty();
		assertThat(NutritionCalculator.totalPrice(items)).isEqualByComparingTo("350");
		assertThat(NutritionCalculator.isPriceComplete(items)).isTrue();
	}

	@Test
	void neverInventsMissingValues() {
		List<RecipeItem> items = List.of(
				item(product(null, "200", null, "5", null), 1, null),
				item(product("80", null, null, "1", null), 2, "80"));

		NutritionResponse nutrition = NutritionCalculator.nutrition(items);

		assertThat(nutrition.calories()).isEqualByComparingTo("200");
		assertThat(nutrition.carbohydrate()).isNull();
		assertThat(nutrition.protein()).isEqualByComparingTo("7");
		assertThat(nutrition.fat()).isNull();
		assertThat(nutrition.complete()).isFalse();
		assertThat(nutrition.missingNutrients()).containsExactly("calories", "carbohydrate", "fat");
		assertThat(NutritionCalculator.totalPrice(items)).isEqualByComparingTo("160");
		assertThat(NutritionCalculator.isPriceComplete(items)).isFalse();
	}

	@Test
	void normalizesTags() {
		assertThat(RecipeService.normalizeTags(List.of("#매콤", "  ##반숙 란 ", "매콤", "", "#")))
				.containsExactly("매콤", "반숙란");
		assertThat(RecipeService.normalizeTag(null)).isNull();
	}
}
