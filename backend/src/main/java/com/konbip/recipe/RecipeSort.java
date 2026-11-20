package com.konbip.recipe;

import java.util.Locale;

import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;

/**
 * 꿀조합 목록 정렬 (FR-RCP-07). 쿼리 파라미터 sort 값: latest, popular, rating, views, bookmarks
 */
public enum RecipeSort {

	LATEST("created_at DESC, recipe_id DESC"),
	/** 인기순 = 좋아요 수 */
	POPULAR("like_count DESC, created_at DESC, recipe_id DESC"),
	/** 별점순. 리뷰가 없는 꿀조합은 뒤로 */
	RATING("CASE WHEN average_rating IS NULL THEN 1 ELSE 0 END, average_rating DESC, review_count DESC, recipe_id DESC"),
	VIEWS("view_count DESC, created_at DESC, recipe_id DESC"),
	BOOKMARKS("bookmark_count DESC, created_at DESC, recipe_id DESC");

	private final String orderBy;

	RecipeSort(String orderBy) {
		this.orderBy = orderBy;
	}

	/** RecipeQueryRepository 가 만든 SQL 컬럼 별칭 기준 ORDER BY 절 */
	public String orderBy() {
		return orderBy;
	}

	public static RecipeSort from(String value) {
		if (value == null || value.isBlank()) {
			return LATEST;
		}
		try {
			return valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT,
					"sort 는 latest, popular, rating, views, bookmarks 중 하나여야 합니다.");
		}
	}
}
