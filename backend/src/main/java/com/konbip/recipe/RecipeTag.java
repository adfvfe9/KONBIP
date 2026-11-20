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

/** 해시태그. '#' 없이 저장한다. (예: 매콤) */
@Getter
@Entity
@Table(name = "recipe_tags")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeTag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tag_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recipe_id", nullable = false)
	private Recipe recipe;

	@Column(nullable = false, length = 30)
	private String name;

	static RecipeTag of(Recipe recipe, String name) {
		RecipeTag tag = new RecipeTag();
		tag.recipe = recipe;
		tag.name = name;
		return tag;
	}
}
