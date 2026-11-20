package com.konbip.product.sync;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Tabecal API 연동 자리.
 * <p>
 * TODO(데이터 관리): API Key 발급 후 실제 응답 형식을 확인하고 fetchAll() 을 구현한다.
 * <ol>
 *   <li>RestClient 로 상품 목록 엔드포인트를 호출 (properties.baseUrl(), properties.apiKey())</li>
 *   <li>응답 1건을 ExternalProduct 로 변환. 없는 값(가격, 영양성분)은 null 그대로 둔다.</li>
 *   <li>페이지가 나뉘어 있으면 끝까지 모아서 반환. 호출 제한(rate limit)을 지킨다.</li>
 * </ol>
 * 응답 형식이 확인되지 않은 상태라 추측으로 매핑하지 않았다.
 * 나머지(저장, 갱신, 단종 처리, 하루 1회 스케줄, 관리자 수동 동기화)는 이미 연결되어 있다.
 */
@Component
@RequiredArgsConstructor
public class TabecalProductSource implements ProductSource {

	private final TabecalProperties properties;

	@Override
	public boolean isConfigured() {
		return properties.isConfigured();
	}

	@Override
	public List<ExternalProduct> fetchAll() {
		throw new UnsupportedOperationException(
				"Tabecal API 응답 매핑이 아직 구현되지 않았습니다. TabecalProductSource.fetchAll() 을 구현해 주세요.");
	}
}
