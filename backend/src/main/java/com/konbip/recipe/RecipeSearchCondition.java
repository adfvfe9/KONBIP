package com.konbip.recipe;

/**
 * 꿀조합 검색 조건. null 인 조건은 적용하지 않는다.
 *
 * @param keyword   제목·설명·해시태그·구성 상품명에서 찾는다 (FR-RCP-06)
 * @param brand     대표 편의점이거나, 그 편의점 상품이 하나라도 들어간 꿀조합
 * @param category  그 카테고리 상품이 하나라도 들어간 꿀조합
 * @param tag       해시태그 정확히 일치 ('#' 없이)
 * @param minRating 평균 별점 하한
 */
public record RecipeSearchCondition(
		String keyword,
		String brand,
		String category,
		String tag,
		Double minRating
) {
}
