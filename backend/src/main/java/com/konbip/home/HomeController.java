package com.konbip.home;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.konbip.global.response.ApiResponse;
import com.konbip.recipe.RecipeQueryRepository;
import com.konbip.recipe.RecipeSort;
import com.konbip.recipe.dto.RecipeSummaryResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HomeController {

	private static final int HOME_LIMIT = 10;

	private final RecipeQueryRepository recipeQueryRepository;

	public record HomeResponse(List<RecipeSummaryResponse> popularRecipes, List<RecipeSummaryResponse> latestRecipes) {
	}

	/**
	 * HOME-001 홈 화면. 인기 꿀조합(좋아요 많은 순) 10개 + 최신 꿀조합 10개. (FR-RNK-01)
	 * 기간별 "인기 상승" 목록은 GET /api/rankings?period=weekly 를 쓴다.
	 */
	@GetMapping("/api/home")
	public ApiResponse<HomeResponse> home() {
		return ApiResponse.ok("홈 화면 조회 성공", new HomeResponse(
				recipeQueryRepository.findTop(RecipeSort.POPULAR, HOME_LIMIT),
				recipeQueryRepository.findTop(RecipeSort.LATEST, HOME_LIMIT)));
	}
}
