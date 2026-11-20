package com.konbip.interaction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;
import com.konbip.interaction.dto.BookmarkResponse;
import com.konbip.interaction.dto.LikeResponse;
import com.konbip.recipe.RecipeRepository;

import lombok.RequiredArgsConstructor;

/**
 * 좋아요/즐겨찾기 (FR-SOC-01, FR-SOC-02).
 * 등록·취소는 멱등이다: 이미 누른 상태에서 또 등록해도 오류 없이 현재 상태를 돌려준다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InteractionService {

	private final InteractionRepository interactionRepository;
	private final RecipeActivityRepository activityRepository;
	private final RecipeRepository recipeRepository;

	public LikeResponse like(Long userId, Long recipeId) {
		add(userId, recipeId, InteractionType.LIKE);
		return new LikeResponse(true, interactionRepository.countByRecipeIdAndType(recipeId, InteractionType.LIKE));
	}

	public LikeResponse unlike(Long userId, Long recipeId) {
		remove(userId, recipeId, InteractionType.LIKE);
		return new LikeResponse(false, interactionRepository.countByRecipeIdAndType(recipeId, InteractionType.LIKE));
	}

	public BookmarkResponse bookmark(Long userId, Long recipeId) {
		if (add(userId, recipeId, InteractionType.BOOKMARK)) {
			activityRepository.save(RecipeActivity.of(recipeId, userId, ActivityType.BOOKMARK));
		}
		return new BookmarkResponse(true,
				interactionRepository.countByRecipeIdAndType(recipeId, InteractionType.BOOKMARK));
	}

	public BookmarkResponse unbookmark(Long userId, Long recipeId) {
		remove(userId, recipeId, InteractionType.BOOKMARK);
		return new BookmarkResponse(false,
				interactionRepository.countByRecipeIdAndType(recipeId, InteractionType.BOOKMARK));
	}

	/** 새로 등록했으면 true */
	private boolean add(Long userId, Long recipeId, InteractionType type) {
		ensureRecipeExists(recipeId);
		if (interactionRepository.existsByUserIdAndRecipeIdAndType(userId, recipeId, type)) {
			return false;
		}
		interactionRepository.saveAndFlush(Interaction.of(userId, recipeId, type));
		return true;
	}

	private void remove(Long userId, Long recipeId, InteractionType type) {
		ensureRecipeExists(recipeId);
		interactionRepository.findByUserIdAndRecipeIdAndType(userId, recipeId, type)
				.ifPresent(interaction -> {
					interactionRepository.delete(interaction);
					interactionRepository.flush();
				});
	}

	private void ensureRecipeExists(Long recipeId) {
		if (!recipeRepository.existsById(recipeId)) {
			throw new BusinessException(ErrorCode.RECIPE_NOT_FOUND);
		}
	}
}
