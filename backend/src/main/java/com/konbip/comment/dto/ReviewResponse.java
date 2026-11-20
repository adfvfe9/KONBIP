package com.konbip.comment.dto;

import java.time.LocalDateTime;

import com.konbip.comment.Comment;

/**
 * @param recipeTitle 마이페이지 "내 리뷰" 목록에서만 채운다. 꿀조합 상세의 리뷰 목록에서는 null
 */
public record ReviewResponse(
		Long reviewId,
		Long recipeId,
		String recipeTitle,
		Long authorId,
		String author,
		int rating,
		String content,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static ReviewResponse from(Comment review) {
		return of(review, null);
	}

	public static ReviewResponse withRecipeTitle(Comment review) {
		return of(review, review.getRecipe().getTitle());
	}

	private static ReviewResponse of(Comment review, String recipeTitle) {
		return new ReviewResponse(
				review.getId(),
				review.getRecipe().getId(),
				recipeTitle,
				review.getUser().getId(),
				review.getUser().getNickname(),
				review.getRating(),
				review.getContent(),
				review.getCreatedAt(),
				review.getUpdatedAt());
	}
}
