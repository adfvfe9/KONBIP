package com.konbip.comment.dto;

import java.time.LocalDateTime;

import com.konbip.comment.Comment;

public record CommentResponse(
		Long commentId,
		Long recipeId,
		Long authorId,
		String author,
		String content,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static CommentResponse from(Comment comment) {
		return new CommentResponse(
				comment.getId(),
				comment.getRecipe().getId(),
				comment.getUser().getId(),
				comment.getUser().getNickname(),
				comment.getContent(),
				comment.getCreatedAt(),
				comment.getUpdatedAt());
	}
}
