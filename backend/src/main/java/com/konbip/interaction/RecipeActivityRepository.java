package com.konbip.interaction;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeActivityRepository extends JpaRepository<RecipeActivity, Long> {

	boolean existsByRecipeIdAndUserIdAndTypeAndCreatedAtGreaterThanEqual(Long recipeId, Long userId,
			ActivityType type, LocalDateTime since);
}
