package com.konbip.comment;

import com.konbip.global.entity.BaseTimeEntity;
import com.konbip.recipe.Recipe;
import com.konbip.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글과 리뷰를 한 테이블(comments)에 type 으로 구분해 저장한다.
 * 리뷰만 별점(1~5)을 가진다.
 */
@Getter
@Entity
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recipe_id", nullable = false)
	private Recipe recipe;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CommentType type;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(columnDefinition = "TINYINT")
	private Integer rating;

	public static Comment comment(User user, Recipe recipe, String content) {
		Comment comment = new Comment();
		comment.user = user;
		comment.recipe = recipe;
		comment.type = CommentType.COMMENT;
		comment.content = content;
		return comment;
	}

	public static Comment review(User user, Recipe recipe, int rating, String content) {
		Comment review = new Comment();
		review.user = user;
		review.recipe = recipe;
		review.type = CommentType.REVIEW;
		review.rating = rating;
		review.content = content;
		return review;
	}

	public boolean isWrittenBy(Long userId) {
		return userId != null && user.getId().equals(userId);
	}

	public void editContent(String content) {
		this.content = content;
	}

	public void editReview(int rating, String content) {
		this.rating = rating;
		this.content = content;
	}
}
