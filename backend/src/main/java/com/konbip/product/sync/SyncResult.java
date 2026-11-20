package com.konbip.product.sync;

/**
 * @param received    받은 상품 수
 * @param created     새로 추가한 상품 수
 * @param updated     정보가 바뀌어 갱신한 상품 수
 * @param deactivated 이번 데이터에 없어서 단종(비활성) 처리한 상품 수
 * @param skipped     ID나 이름이 없어서 건너뛴 수
 */
public record SyncResult(int received, int created, int updated, int deactivated, int skipped) {
}
