package com.konbip.interaction;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

	boolean existsByUserIdAndRecipeIdAndType(Long userId, Long recipeId, InteractionType type);

	Optional<Interaction> findByUserIdAndRecipeIdAndType(Long userId, Long recipeId, InteractionType type);

	long countByRecipeIdAndType(Long recipeId, InteractionType type);
}
