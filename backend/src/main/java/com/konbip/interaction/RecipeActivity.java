package com.konbip.interaction;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 조회 및 즐겨찾기 이력 (recipe_views). 기간별 랭킹 집계에 쓴다.
 * 즐겨찾기를 해제해도 이력은 남는다. (그 기간에 즐겨찾기가 눌린 횟수)
 */
@Getter
@Entity
@Table(name = "recipe_views")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeActivity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "activity_id")
	private Long id;

	@Column(name = "recipe_id", nullable = false)
	private Long recipeId;

	/** 비회원 조회면 NULL */
	@Column(name = "user_id")
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ActivityType type;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static RecipeActivity of(Long recipeId, Long userId, ActivityType type) {
		RecipeActivity activity = new RecipeActivity();
		activity.recipeId = recipeId;
		activity.userId = userId;
		activity.type = type;
		return activity;
	}
}
