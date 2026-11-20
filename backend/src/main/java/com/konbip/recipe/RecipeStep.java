package com.konbip.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** 레시피 단계 (STEP 1, STEP 2 ...) */
@Getter
@Entity
@Table(name = "recipe_steps")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeStep {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "step_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recipe_id", nullable = false)
	private Recipe recipe;

	/** 1부터 시작 */
	@Column(name = "step_order", nullable = false)
	private int stepOrder;

	@Column(nullable = false, length = 500)
	private String content;

	static RecipeStep of(Recipe recipe, int stepOrder, String content) {
		RecipeStep step = new RecipeStep();
		step.recipe = recipe;
		step.update(stepOrder, content);
		return step;
	}

	void update(int stepOrder, String content) {
		this.stepOrder = stepOrder;
		this.content = content;
	}
}
