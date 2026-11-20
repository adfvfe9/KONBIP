package com.konbip.recipe.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 꿀조합 영양성분 합계. (FR-CMB-04, FR-CMB-05)
 * <ul>
 *   <li>각 값 = Σ(상품 1개 기준 값 × 수량). 정보가 있는 상품만 더한다.</li>
 *   <li>그 영양성분 정보가 있는 상품이 하나도 없으면 null.</li>
 *   <li>complete=false 면 일부 상품의 정보가 빠진 합계다. missingNutrients 로 어떤 항목인지 알려준다.
 *       프론트에서 "일부 상품의 영양정보가 없어 실제보다 적을 수 있어요" 같은 안내를 띄운다.</li>
 * </ul>
 *
 * @param missingNutrients calories, carbohydrate, protein, fat 중 일부 상품에서 빠진 항목 이름
 */
public record NutritionResponse(
		BigDecimal calories,
		BigDecimal carbohydrate,
		BigDecimal protein,
		BigDecimal fat,
		boolean complete,
		List<String> missingNutrients
) {
}
