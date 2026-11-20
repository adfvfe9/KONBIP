package com.konbip.product;

import java.math.BigDecimal;
import java.util.Objects;

import com.konbip.global.entity.BaseTimeEntity;
import com.konbip.product.sync.ExternalProduct;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_id")
	private Long id;

	/** 외부 API(Tabecal) 상품 식별값 */
	@Column(name = "external_id", nullable = false, unique = true)
	private String externalId;

	@Column(nullable = false)
	private String name;

	@Column(length = 100)
	private String brand;

	@Column(length = 100)
	private String category;

	/** API 에 가격이 없으면 NULL. 조합 등록 시 사용자가 직접 입력한다. */
	@Column(precision = 10, scale = 2)
	private BigDecimal price;

	@Column(precision = 10, scale = 2)
	private BigDecimal calories;

	@Column(precision = 10, scale = 2)
	private BigDecimal carbohydrate;

	@Column(precision = 10, scale = 2)
	private BigDecimal protein;

	@Column(precision = 10, scale = 2)
	private BigDecimal fat;

	/** false 면 단종(API 에서 사라짐). 새 조합에는 담을 수 없다. */
	@Column(nullable = false)
	private boolean active;

	public static Product from(ExternalProduct source) {
		Product product = new Product();
		product.externalId = source.externalId();
		product.active = true;
		product.apply(source);
		return product;
	}

	/** 외부 데이터로 갱신한다. 실제로 바뀐 값이 있으면 true. */
	public boolean updateFrom(ExternalProduct source) {
		boolean changed = !active
				|| !Objects.equals(name, source.name())
				|| !Objects.equals(brand, source.brand())
				|| !Objects.equals(category, source.category())
				|| !sameNumber(price, source.price())
				|| !sameNumber(calories, source.calories())
				|| !sameNumber(carbohydrate, source.carbohydrate())
				|| !sameNumber(protein, source.protein())
				|| !sameNumber(fat, source.fat());
		if (changed) {
			apply(source);
			this.active = true;
		}
		return changed;
	}

	public void deactivate() {
		this.active = false;
	}

	private void apply(ExternalProduct source) {
		this.name = source.name();
		this.brand = source.brand();
		this.category = source.category();
		this.price = source.price();
		this.calories = source.calories();
		this.carbohydrate = source.carbohydrate();
		this.protein = source.protein();
		this.fat = source.fat();
	}

	private static boolean sameNumber(BigDecimal a, BigDecimal b) {
		if (a == null || b == null) {
			return a == b;
		}
		return a.compareTo(b) == 0;
	}
}
