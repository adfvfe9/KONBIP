package com.konbip.product.sync;

import java.util.List;

/**
 * 상품 데이터 공급원. 지금은 Tabecal API 하나(TabecalProductSource)만 있다.
 */
public interface ProductSource {

	/** API 키 등 설정이 되어 있어서 호출할 수 있는 상태인지 */
	boolean isConfigured();

	/** 전체 상품 목록을 가져온다. 실패하면 예외를 던진다. */
	List<ExternalProduct> fetchAll();
}
