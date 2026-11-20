package com.konbip.recipe.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 꿀조합 등록/수정 요청. (multipart 의 "recipe" 파트 또는 JSON 본문)
 * <pre>
 * {
 *   "title": "몽고탄멘 조합",
 *   "description": "매콤한 몽고탄멘에 ...",
 *   "brand": "세븐일레븐",
 *   "tags": ["매콤", "반숙란"],
 *   "steps": ["몽고탄멘을 조리법대로 조리한다", "반숙 계란과 장조림을 올린다"],
 *   "items": [
 *     { "productId": 1, "quantity": 1, "customPrice": null },
 *     { "productId": 3, "quantity": 2, "customPrice": 300 }
 *   ]
 * }
 * </pre>
 * customPrice 는 상품에 가격 정보가 없을 때만 쓰인다. 같은 상품을 여러 번 보내면 수량을 합친다.
 */
public record RecipeRequest(
		@NotBlank(message = "제목을 입력해 주세요.")
		@Size(min = 2, max = 50, message = "제목은 2~50자여야 합니다.")
		String title,

		@Size(max = 1000, message = "설명은 1,000자 이하여야 합니다.")
		String description,

		@Size(max = 100)
		String brand,

		@Size(max = 10, message = "해시태그는 10개까지 넣을 수 있습니다.")
		List<@NotBlank @Size(max = 30, message = "해시태그는 30자 이하여야 합니다.") String> tags,

		@Size(max = 20, message = "레시피 단계는 20개까지 넣을 수 있습니다.")
		List<@NotBlank @Size(max = 500, message = "레시피 단계는 500자 이하여야 합니다.") String> steps,

		@NotEmpty(message = "상품을 1개 이상 담아 주세요.")
		@Size(max = 30, message = "상품은 30종류까지 담을 수 있습니다.")
		List<@Valid @NotNull Item> items
) {

	public record Item(
			@NotNull Long productId,
			@NotNull @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
			@Max(value = 10, message = "한 상품은 최대 10개까지 담을 수 있습니다.")
			Integer quantity,
			@DecimalMin(value = "0", message = "가격은 0 이상이어야 합니다.")
			@Digits(integer = 8, fraction = 2)
			BigDecimal customPrice
	) {
	}
}
