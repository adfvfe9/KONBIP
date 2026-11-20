package com.konbip.recipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.konbip.global.entity.BaseTimeEntity;
import com.konbip.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 꿀조합(레시피). 구성 상품(items), 레시피 단계(steps), 해시태그(tags)를 가진다.
 */
@Getter
@Entity
@Table(name = "recipes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recipe extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recipe_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	/** 대표 편의점 (세븐일레븐, 패밀리마트, 로손 등) */
	@Column(length = 100)
	private String brand;

	/** 이미지 바이트는 ImageStore 가 다룬다. 여기서는 이미지 유무 확인용으로 읽기만 한다. */
	@Column(name = "image_type", insertable = false, updatable = false)
	private String imageType;

	@OrderBy("id ASC")
	@OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RecipeItem> items = new ArrayList<>();

	@OrderBy("stepOrder ASC")
	@OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RecipeStep> steps = new ArrayList<>();

	@OrderBy("id ASC")
	@OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RecipeTag> tags = new ArrayList<>();

	public static Recipe create(User user, String title, String description, String brand) {
		Recipe recipe = new Recipe();
		recipe.user = user;
		recipe.updateInfo(title, description, brand);
		return recipe;
	}

	public void updateInfo(String title, String description, String brand) {
		this.title = title;
		this.description = description;
		this.brand = brand;
	}

	public boolean isWrittenBy(Long userId) {
		return userId != null && user.getId().equals(userId);
	}

	public boolean hasImage() {
		return imageType != null;
	}

	/**
	 * 구성 상품을 바꾼다. 이미 있는 상품은 수량/가격만 고치고, 빠진 상품은 지우고, 새 상품은 추가한다.
	 * (전부 지우고 다시 넣으면 uk_recipe_product 유니크 키와 충돌할 수 있어서 이렇게 한다.)
	 */
	public void replaceItems(List<RecipeItem.Draft> drafts) {
		Map<Long, RecipeItem.Draft> byProduct = new LinkedHashMap<>();
		for (RecipeItem.Draft draft : drafts) {
			byProduct.put(draft.product().getId(), draft);
		}
		items.removeIf(item -> !byProduct.containsKey(item.getProduct().getId()));
		for (RecipeItem item : items) {
			RecipeItem.Draft draft = byProduct.remove(item.getProduct().getId());
			item.update(draft.quantity(), draft.unitPrice());
		}
		for (RecipeItem.Draft draft : byProduct.values()) {
			items.add(RecipeItem.of(this, draft));
		}
	}

	public void replaceSteps(List<String> contents) {
		int common = Math.min(steps.size(), contents.size());
		for (int i = 0; i < common; i++) {
			steps.get(i).update(i + 1, contents.get(i));
		}
		while (steps.size() > contents.size()) {
			steps.remove(steps.size() - 1);
		}
		for (int i = common; i < contents.size(); i++) {
			steps.add(RecipeStep.of(this, i + 1, contents.get(i)));
		}
	}

	public void replaceTags(List<String> names) {
		Set<String> wanted = new HashSet<>(names);
		tags.removeIf(tag -> !wanted.contains(tag.getName()));
		Set<String> current = new HashSet<>();
		tags.forEach(tag -> current.add(tag.getName()));
		for (String name : names) {
			if (current.add(name)) {
				tags.add(RecipeTag.of(this, name));
			}
		}
	}
}
