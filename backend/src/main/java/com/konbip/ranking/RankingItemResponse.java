package com.konbip.ranking;

import com.konbip.recipe.dto.RecipeSummaryResponse;

/**
 * @param rank  1부터 시작하는 순위
 * @param score 기간 내 조회수 / 즐겨찾기 수 / 평균 별점
 */
public record RankingItemResponse(int rank, Number score, RecipeSummaryResponse recipe) {
}
