package com.konbip.recipe;

import java.math.BigDecimal;

import com.konbip.product.Product;

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

/**
 * 꿀조합 구성 상품.
 * 영양성분(calories 등)은 등록 당시 상품 1개 기준 값을 복사해 둔다. 합계 = 값 × quantity.
 * 그래서 나중에 상품 데이터가 바뀌거나 단종되어도 등록한 꿀조합의 영양정보는 그대로 유지된다.
 */
@Getter
@Entity
@Table(name = "recipe_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeItem {

	/** 저장 전 검증을 마친 구성 상품 정보 */
	public record Draft(Product product, int quantity, BigDecimal unitPrice) {
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recipe_item_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recipe_id", nullable = false)
	private Recipe recipe;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false)
	private int quantity;

	/** 상품 가격(API) 또는 사용자가 이 꿀조합에서 직접 입력한 가격. 둘 다 없으면 NULL */
	@Column(name = "unit_price", precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Column(precision = 10, scale = 2)
	private BigDecimal calories;

	@Column(precision = 10, scale = 2)
	private BigDecimal carbohydrate;

	@Column(precision = 10, scale = 2)
	private BigDecimal protein;

	@Column(precision = 10, scale = 2)
	private BigDecimal fat;

	static RecipeItem of(Recipe recipe, Draft draft) {
		RecipeItem item = new RecipeItem();
		item.recipe = recipe;
		item.product = draft.product();
		item.quantity = draft.quantity();
		item.unitPrice = draft.unitPrice();
		item.calories = draft.product().getCalories();
		item.carbohydrate = draft.product().getCarbohydrate();
		item.protein = draft.product().getProtein();
		item.fat = draft.product().getFat();
		return item;
	}

	void update(int quantity, BigDecimal unitPrice) {
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}
}
