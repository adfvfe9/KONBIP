package com.konbip.comment;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.konbip.comment.dto.CommentRequest;
import com.konbip.comment.dto.CommentResponse;
import com.konbip.comment.dto.ReviewRequest;
import com.konbip.comment.dto.ReviewResponse;
import com.konbip.global.response.ApiResponse;
import com.konbip.global.response.PageResponse;
import com.konbip.global.response.Pages;
import com.konbip.global.security.LoginUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CommentController {

	static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

	private final CommentService commentService;

	/** COMMENT-001 댓글 목록 (최신순) */
	@GetMapping("/api/recipes/{recipeId}/comments")
	public ApiResponse<PageResponse<CommentResponse>> getComments(
			@PathVariable Long recipeId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ApiResponse.ok(commentService.getComments(recipeId, Pages.of(page, size, NEWEST_FIRST)));
	}

	/** COMMENT-002 댓글 작성 */
	@PostMapping("/api/recipes/{recipeId}/comments")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<CommentResponse> createComment(@LoginUser Long userId, @PathVariable Long recipeId,
			@Valid @RequestBody CommentRequest request) {
		return ApiResponse.ok("댓글이 등록되었습니다.", commentService.createComment(userId, recipeId, request));
	}

	/** COMMENT-003 댓글 수정 */
	@PatchMapping("/api/comments/{commentId}")
	public ApiResponse<CommentResponse> updateComment(@LoginUser Long userId, @PathVariable Long commentId,
			@Valid @RequestBody CommentRequest request) {
		return ApiResponse.ok("댓글이 수정되었습니다.", commentService.updateComment(userId, commentId, request));
	}

	/** COMMENT-004 댓글 삭제 */
	@DeleteMapping("/api/comments/{commentId}")
	public ApiResponse<Void> deleteComment(@LoginUser Long userId, @PathVariable Long commentId) {
		commentService.deleteComment(userId, commentId);
		return ApiResponse.ok("댓글이 삭제되었습니다.", null);
	}

	/** REVIEW-001 리뷰 목록 (최신순) */
	@GetMapping("/api/recipes/{recipeId}/reviews")
	public ApiResponse<PageResponse<ReviewResponse>> getReviews(
			@PathVariable Long recipeId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ApiResponse.ok(commentService.getReviews(recipeId, Pages.of(page, size, NEWEST_FIRST)));
	}

	/** REVIEW-002 리뷰 작성 */
	@PostMapping("/api/recipes/{recipeId}/reviews")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ReviewResponse> createReview(@LoginUser Long userId, @PathVariable Long recipeId,
			@Valid @RequestBody ReviewRequest request) {
		return ApiResponse.ok("리뷰가 등록되었습니다.", commentService.createReview(userId, recipeId, request));
	}

	/** REVIEW-003 리뷰 수정 */
	@PatchMapping("/api/reviews/{reviewId}")
	public ApiResponse<ReviewResponse> updateReview(@LoginUser Long userId, @PathVariable Long reviewId,
			@Valid @RequestBody ReviewRequest request) {
		return ApiResponse.ok("리뷰가 수정되었습니다.", commentService.updateReview(userId, reviewId, request));
	}

	/** REVIEW-004 리뷰 삭제 */
	@DeleteMapping("/api/reviews/{reviewId}")
	public ApiResponse<Void> deleteReview(@LoginUser Long userId, @PathVariable Long reviewId) {
		commentService.deleteReview(userId, reviewId);
		return ApiResponse.ok("리뷰가 삭제되었습니다.", null);
	}
}
