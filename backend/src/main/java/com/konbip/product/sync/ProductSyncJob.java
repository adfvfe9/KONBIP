package com.konbip.product.sync;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 외부 API 호출 → DB 반영.
 * 외부 호출은 트랜잭션 밖에서 하고, DB 반영만 ProductSyncService 트랜잭션 안에서 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSyncJob {

	private final ProductSource productSource;
	private final ProductSyncService productSyncService;

	/** ADMIN-005 수동 동기화 */
	public SyncResult runNow() {
		if (!productSource.isConfigured()) {
			throw new BusinessException(ErrorCode.PRODUCT_SYNC_NOT_CONFIGURED);
		}
		List<ExternalProduct> feed;
		try {
			feed = productSource.fetchAll();
		} catch (RuntimeException e) {
			log.error("Product fetch failed. Existing data is kept.", e);
			throw new BusinessException(ErrorCode.PRODUCT_SYNC_FAILED);
		}
		return productSyncService.apply(feed);
	}

	/**
	 * 하루 1회 자동 동기화. konbip.product-sync.enabled=true 일 때만 스케줄이 켜진다. (SchedulingConfig)
	 * 실패해도 기존 데이터는 그대로 두고 로그만 남긴다. (FR-PRD-01, NFR-AVL-01)
	 */
	@Scheduled(cron = "${konbip.product-sync.cron}", zone = "${konbip.product-sync.zone}")
	public void scheduledSync() {
		try {
			runNow();
		} catch (BusinessException e) {
			log.warn("Scheduled product sync skipped: {}", e.getMessage());
		}
	}
}
