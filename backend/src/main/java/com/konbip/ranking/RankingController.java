package com.konbip.ranking;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.konbip.global.response.ApiResponse;
import com.konbip.global.response.PageResponse;
import com.konbip.global.response.Pages;
import com.konbip.recipe.RecipeQueryRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RankingController {

	private final RecipeQueryRepository recipeQueryRepository;
	private final RankingProperties rankingProperties;

	/**
	 * RANKING-001 기간별 꿀조합 랭킹.
	 * <ul>
	 *   <li>period: daily(최근 1일), weekly(최근 7일, 기본), monthly(최근 30일), all(전체)</li>
	 *   <li>type: views(조회수, 기본), bookmarks(즐겨찾기 수), rating(평균 별점, 기간 내 리뷰 N개 이상)</li>
	 * </ul>
	 */
	@GetMapping("/api/rankings")
	public ApiResponse<PageResponse<RankingItemResponse>> ranking(
			@RequestParam(required = false) String period,
			@RequestParam(required = false) String type,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		RankingPeriod rankingPeriod = RankingPeriod.from(period);
		return ApiResponse.ok("랭킹 조회 성공", recipeQueryRepository.ranking(
				RankingType.from(type),
				rankingPeriod.since(LocalDateTime.now()),
				rankingProperties.minReviewCount(),
				Pages.of(page, size)));
	}
}
