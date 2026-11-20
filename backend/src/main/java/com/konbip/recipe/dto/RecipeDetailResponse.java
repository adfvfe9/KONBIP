package com.konbip.recipe.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.konbip.recipe.NutritionCalculator;
import com.konbip.recipe.Recipe;
import com.konbip.recipe.RecipeItem;

/**
 * 꿀조합 상세. (FR-RCP-08, API 명세서 RECIPE-002)
 *
 * @param totalPrice    가격 정보가 있는 상품만 더한 합계 (가격 정보가 하나도 없으면 null)
 * @param priceComplete false 면 가격이 없는 상품이 있어 합계에서 빠졌다는 뜻
 * @param isReviewed    로그인한 회원이 이미 리뷰를 썼는지 (리뷰 작성 버튼 숨김용)
 * @param isMine        로그인한 회원이 작성자인지 (수정/삭제 버튼 표시용)
 */
public record RecipeDetailResponse(
		Long recipeId,
		String title,
		String description,
		String brand,
		String imageUrl,
		Long authorId,
		String author,
		List<String> tags,
		List<Step> steps,
		List<RecipeItemResponse> items,
		BigDecimal totalPrice,
		boolean priceComplete,
		BigDecimal totalCalories,
		NutritionResponse nutrition,
		Double averageRating,
		long reviewCount,
		long commentCount,
		long likeCount,
		long bookmarkCount,
		long viewCount,
		boolean isLiked,
		boolean isBookmarked,
		boolean isReviewed,
		boolean isMine,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public record Step(int order, String content) {
	}

	public record ViewerState(boolean liked, boolean bookmarked, boolean reviewed, boolean mine) {
	}

	public static RecipeDetailResponse of(Recipe recipe, RecipeSummaryResponse summary, ViewerState viewer) {
		List<RecipeItem> items = recipe.getItems();
		NutritionResponse nutrition = NutritionCalculator.nutrition(items);
		return new RecipeDetailResponse(
				recipe.getId(),
				recipe.getTitle(),
				recipe.getDescription(),
				recipe.getBrand(),
				summary.imageUrl(),
				recipe.getUser().getId(),
				recipe.getUser().getNickname(),
				recipe.getTags().stream().map(tag -> tag.getName()).toList(),
				recipe.getSteps().stream().map(step -> new Step(step.getStepOrder(), step.getContent())).toList(),
				items.stream().map(RecipeItemResponse::from).toList(),
				NutritionCalculator.totalPrice(items),
				NutritionCalculator.isPriceComplete(items),
				nutrition.calories(),
				nutrition,
				summary.averageRating(),
				summary.reviewCount(),
				summary.commentCount(),
				summary.likeCount(),
				summary.bookmarkCount(),
				summary.viewCount(),
				viewer.liked(),
				viewer.bookmarked(),
				viewer.reviewed(),
				viewer.mine(),
				recipe.getCreatedAt(),
				recipe.getUpdatedAt());
	}
}
