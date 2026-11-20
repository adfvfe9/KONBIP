package com.konbip.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.konbip.comment.dto.CommentRequest;
import com.konbip.comment.dto.CommentResponse;
import com.konbip.comment.dto.ReviewRequest;
import com.konbip.comment.dto.ReviewResponse;
import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;
import com.konbip.global.response.PageResponse;
import com.konbip.recipe.Recipe;
import com.konbip.recipe.RecipeRepository;
import com.konbip.user.User;
import com.konbip.user.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 댓글 (FR-SOC-03, 04) 과 리뷰·별점 (FR-SOC-05, 06).
 * 리뷰 정책: 회원당 꿀조합 1개, 본인 꿀조합에는 작성 불가.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

	private final CommentRepository commentRepository;
	private final RecipeRepository recipeRepository;
	private final UserRepository userRepository;

	// ---------------------------------------------------------------- 댓글

	public PageResponse<CommentResponse> getComments(Long recipeId, Pageable pageable) {
		ensureRecipeExists(recipeId);
		return PageResponse.of(commentRepository.findByRecipe(recipeId, CommentType.COMMENT, pageable),
				CommentResponse::from);
	}

	@Transactional
	public CommentResponse createComment(Long userId, Long recipeId, CommentRequest request) {
		Recipe recipe = findRecipe(recipeId);
		Comment comment = commentRepository.save(Comment.comment(findUser(userId), recipe, request.content().trim()));
		return CommentResponse.from(comment);
	}

	@Transactional
	public CommentResponse updateComment(Long userId, Long commentId, CommentRequest request) {
		Comment comment = findOwned(userId, commentId, CommentType.COMMENT);
		comment.editContent(request.content().trim());
		commentRepository.flush();
		return CommentResponse.from(comment);
	}

	@Transactional
	public void deleteComment(Long userId, Long commentId) {
		commentRepository.delete(findOwned(userId, commentId, CommentType.COMMENT));
	}

	// ---------------------------------------------------------------- 리뷰

	public PageResponse<ReviewResponse> getReviews(Long recipeId, Pageable pageable) {
		ensureRecipeExists(recipeId);
		return PageResponse.of(commentRepository.findByRecipe(recipeId, CommentType.REVIEW, pageable),
				ReviewResponse::from);
	}

	@Transactional
	public ReviewResponse createReview(Long userId, Long recipeId, ReviewRequest request) {
		Recipe recipe = findRecipe(recipeId);
		if (recipe.isWrittenBy(userId)) {
			throw new BusinessException(ErrorCode.CANNOT_REVIEW_OWN_RECIPE);
		}
		if (commentRepository.existsByUserAndRecipe(userId, recipeId, CommentType.REVIEW)) {
			throw new BusinessException(ErrorCode.DUPLICATE_REVIEW);
		}
		Comment review = commentRepository.save(
				Comment.review(findUser(userId), recipe, request.rating(), contentOf(request)));
		return ReviewResponse.from(review);
	}

	@Transactional
	public ReviewResponse updateReview(Long userId, Long reviewId, ReviewRequest request) {
		Comment review = findOwned(userId, reviewId, CommentType.REVIEW);
		review.editReview(request.rating(), contentOf(request));
		commentRepository.flush();
		return ReviewResponse.from(review);
	}

	@Transactional
	public void deleteReview(Long userId, Long reviewId) {
		commentRepository.delete(findOwned(userId, reviewId, CommentType.REVIEW));
	}

	/** MY-004 내가 작성한 리뷰 */
	public PageResponse<ReviewResponse> getMyReviews(Long userId, Pageable pageable) {
		return PageResponse.of(commentRepository.findByUser(userId, CommentType.REVIEW, pageable),
				ReviewResponse::withRecipeTitle);
	}

	// ---------------------------------------------------------------- 관리자

	/** ADMIN-004 댓글·리뷰 삭제 */
	@Transactional
	public void deleteByAdmin(Long commentId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
		commentRepository.delete(comment);
	}

	// ----------------------------------------------------------------

	private Comment findOwned(Long userId, Long id, CommentType type) {
		ErrorCode notFound = type == CommentType.REVIEW ? ErrorCode.REVIEW_NOT_FOUND : ErrorCode.COMMENT_NOT_FOUND;
		Comment comment = commentRepository.findByIdAndType(id, type)
				.orElseThrow(() -> new BusinessException(notFound));
		if (!comment.isWrittenBy(userId)) {
			throw new BusinessException(ErrorCode.NOT_COMMENT_AUTHOR);
		}
		return comment;
	}

	private Recipe findRecipe(Long recipeId) {
		return recipeRepository.findById(recipeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.RECIPE_NOT_FOUND));
	}

	private void ensureRecipeExists(Long recipeId) {
		if (!recipeRepository.existsById(recipeId)) {
			throw new BusinessException(ErrorCode.RECIPE_NOT_FOUND);
		}
	}

	private User findUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	private static String contentOf(ReviewRequest request) {
		return request.content() == null ? "" : request.content().trim();
	}
}
