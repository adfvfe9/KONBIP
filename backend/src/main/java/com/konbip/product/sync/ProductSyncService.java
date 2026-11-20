package com.konbip.product.sync;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.konbip.product.Product;
import com.konbip.product.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 외부 상품 데이터를 DB 에 반영한다. (FR-PRD-01, FR-PRD-06)
 * <ul>
 *   <li>새 상품은 추가, 바뀐 상품은 갱신</li>
 *   <li>이번 데이터에 없는 상품은 삭제하지 않고 비활성(단종) 처리 → 기존 꿀조합의 참조 유지</li>
 *   <li>받은 데이터가 비어 있으면 API 장애로 보고 비활성 처리를 하지 않는다</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSyncService {

	private final ProductRepository productRepository;

	@Transactional
	public SyncResult apply(List<ExternalProduct> feed) {
		Map<String, Product> existing = new HashMap<>();
		for (Product product : productRepository.findAll()) {
			existing.put(product.getExternalId(), product);
		}

		Set<String> seen = new HashSet<>();
		int created = 0;
		int updated = 0;
		int skipped = 0;
		for (ExternalProduct source : feed) {
			if (isBlank(source.externalId()) || isBlank(source.name()) || !seen.add(source.externalId())) {
				skipped++;
				continue;
			}
			Product product = existing.get(source.externalId());
			if (product == null) {
				productRepository.save(Product.from(source));
				created++;
			} else if (product.updateFrom(source)) {
				updated++;
			}
		}

		int deactivated = 0;
		if (!seen.isEmpty()) {
			for (Product product : existing.values()) {
				if (product.isActive() && !seen.contains(product.getExternalId())) {
					product.deactivate();
					deactivated++;
				}
			}
		}

		SyncResult result = new SyncResult(feed.size(), created, updated, deactivated, skipped);
		log.info("Product sync applied: {}", result);
		return result;
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
