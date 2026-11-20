package com.konbip.comment;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	@Query(value = "SELECT c FROM Comment c JOIN FETCH c.user WHERE c.recipe.id = :recipeId AND c.type = :type",
			countQuery = "SELECT COUNT(c) FROM Comment c WHERE c.recipe.id = :recipeId AND c.type = :type")
	Page<Comment> findByRecipe(@Param("recipeId") Long recipeId, @Param("type") CommentType type, Pageable pageable);

	@Query(value = "SELECT c FROM Comment c JOIN FETCH c.recipe WHERE c.user.id = :userId AND c.type = :type",
			countQuery = "SELECT COUNT(c) FROM Comment c WHERE c.user.id = :userId AND c.type = :type")
	Page<Comment> findByUser(@Param("userId") Long userId, @Param("type") CommentType type, Pageable pageable);

	@Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.id = :id AND c.type = :type")
	Optional<Comment> findByIdAndType(@Param("id") Long id, @Param("type") CommentType type);

	@Query("""
			SELECT COUNT(c) > 0 FROM Comment c
			WHERE c.user.id = :userId AND c.recipe.id = :recipeId AND c.type = :type
			""")
	boolean existsByUserAndRecipe(@Param("userId") Long userId, @Param("recipeId") Long recipeId,
			@Param("type") CommentType type);
}
