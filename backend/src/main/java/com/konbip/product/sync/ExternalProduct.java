package com.konbip.product.sync;

import java.math.BigDecimal;

/**
 * 외부 상품 데이터 1건. ProductSource 구현체가 API 응답을 이 형태로 바꿔서 넘긴다.
 * 제공되지 않는 값은 null 로 둔다. (임의로 채우지 않는다 - 기획서 9.5)
 */
public record ExternalProduct(
		String externalId,
		String name,
		String brand,
		String category,
		BigDecimal price,
		BigDecimal calories,
		BigDecimal carbohydrate,
		BigDecimal protein,
		BigDecimal fat
) {
}
