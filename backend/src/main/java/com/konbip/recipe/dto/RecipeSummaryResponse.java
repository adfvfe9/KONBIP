package com.konbip.recipe.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 꿀조합 카드 (목록, 홈, 랭킹, 마이페이지 공통). FR-RCP-05
 *
 * @param imageUrl      이미지가 없으면 null → 프론트에서 기본 이미지 표시
 * @param totalCalories 구성 상품 칼로리 합계. 칼로리 정보가 하나도 없으면 null
 * @param averageRating 소수 첫째 자리까지. 리뷰가 없으면 null
 */
public record RecipeSummaryResponse(
		Long recipeId,
		String title,
		String brand,
		String imageUrl,
		Long authorId,
		String author,
		List<String> tags,
		BigDecimal totalCalories,
		long likeCount,
		long bookmarkCount,
		long commentCount,
		long reviewCount,
		Double averageRating,
		long viewCount,
		LocalDateTime createdAt
) {

	public static String imageUrl(Long recipeId) {
		return "/api/recipes/" + recipeId + "/image";
	}
}
