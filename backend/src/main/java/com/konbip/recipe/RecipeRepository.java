package com.konbip.recipe;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

	@Query("SELECT r FROM Recipe r JOIN FETCH r.user WHERE r.id = :id")
	Optional<Recipe> findWithUserById(@Param("id") Long id);

	/**
	 * 회원 탈퇴 시 그 회원의 꿀조합을 모두 지운다.
	 * 구성 상품·단계·태그·좋아요·댓글·조회 이력은 DB 의 ON DELETE CASCADE 로 함께 지워진다.
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = "DELETE FROM recipes WHERE user_id = :userId", nativeQuery = true)
	int deleteAllByUserId(@Param("userId") Long userId);
}
