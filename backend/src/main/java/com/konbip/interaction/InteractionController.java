package com.konbip.interaction;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.konbip.global.response.ApiResponse;
import com.konbip.global.security.LoginUser;
import com.konbip.interaction.dto.BookmarkResponse;
import com.konbip.interaction.dto.LikeResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recipes/{recipeId}")
@RequiredArgsConstructor
public class InteractionController {

	private final InteractionService interactionService;

	/** INTERACTION-001 좋아요 등록 */
	@PostMapping("/like")
	public ApiResponse<LikeResponse> like(@LoginUser Long userId, @PathVariable Long recipeId) {
		return ApiResponse.ok(interactionService.like(userId, recipeId));
	}

	/** INTERACTION-002 좋아요 취소 */
	@DeleteMapping("/like")
	public ApiResponse<LikeResponse> unlike(@LoginUser Long userId, @PathVariable Long recipeId) {
		return ApiResponse.ok(interactionService.unlike(userId, recipeId));
	}

	/** INTERACTION-003 즐겨찾기 등록 */
	@PostMapping("/bookmark")
	public ApiResponse<BookmarkResponse> bookmark(@LoginUser Long userId, @PathVariable Long recipeId) {
		return ApiResponse.ok(interactionService.bookmark(userId, recipeId));
	}

	/** INTERACTION-004 즐겨찾기 취소 */
	@DeleteMapping("/bookmark")
	public ApiResponse<BookmarkResponse> unbookmark(@LoginUser Long userId, @PathVariable Long recipeId) {
		return ApiResponse.ok(interactionService.unbookmark(userId, recipeId));
	}
}
