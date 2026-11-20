package com.konbip.dev;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.konbip.global.response.PageResponse;
import com.konbip.product.ProductRepository;
import com.konbip.ranking.RankingItemResponse;
import com.konbip.ranking.RankingType;
import com.konbip.recipe.RecipeQueryRepository;
import com.konbip.recipe.RecipeRepository;
import com.konbip.user.UserRepository;

/** 빈 DB 에서 로컬 샘플 데이터가 끝까지 들어가는지 확인한다. */
@ActiveProfiles("test")
@SpringBootTest(properties = {
		"konbip.seed.enabled=true",
		"spring.datasource.url=jdbc:h2:mem:seedtest;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
})
class LocalDataSeederTest {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RecipeRepository recipeRepository;

	@Autowired
	private RecipeQueryRepository recipeQueryRepository;

	@Test
	void seedsSampleData() {
		assertThat(productRepository.count()).isEqualTo(15);
		assertThat(userRepository.count()).isEqualTo(4);
		assertThat(recipeRepository.count()).isEqualTo(5);
		assertThat(userRepository.findByEmail("admin@konbip.dev").orElseThrow().getRole().name()).isEqualTo("ADMIN");

		// 몽고탄멘 조합은 리뷰 3개라 별점 랭킹에 들어간다
		PageResponse<RankingItemResponse> rating = recipeQueryRepository.ranking(
				RankingType.RATING, LocalDateTime.now().minusDays(1), 3, PageRequest.of(0, 10));
		assertThat(rating.content()).extracting(item -> item.recipe().title()).containsExactly("몽고탄멘 조합");
	}
}
