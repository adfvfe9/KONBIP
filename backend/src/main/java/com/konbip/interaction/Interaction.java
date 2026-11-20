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

/** 좋아요 / 즐겨찾기. (회원, 꿀조합, 종류) 조합은 하나만 존재한다. */
@Getter
@Entity
@Table(name = "interactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "interaction_id")
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "recipe_id", nullable = false)
	private Long recipeId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private InteractionType type;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static Interaction of(Long userId, Long recipeId, InteractionType type) {
		Interaction interaction = new Interaction();
		interaction.userId = userId;
		interaction.recipeId = recipeId;
		interaction.type = type;
		return interaction;
	}
}
