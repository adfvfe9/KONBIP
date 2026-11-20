package com.konbip.global.response;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * page(0부터), size 쿼리 파라미터를 안전한 범위로 맞춘다. (NFR-PRF-03: 한 번에 20개 내외)
 */
public final class Pages {

	public static final int DEFAULT_SIZE = 20;
	public static final int MAX_SIZE = 100;

	private Pages() {
	}

	public static PageRequest of(int page, int size) {
		return PageRequest.of(Math.max(page, 0), clampSize(size));
	}

	public static PageRequest of(int page, int size, Sort sort) {
		return PageRequest.of(Math.max(page, 0), clampSize(size), sort);
	}

	private static int clampSize(int size) {
		if (size < 1) {
			return DEFAULT_SIZE;
		}
		return Math.min(size, MAX_SIZE);
	}
}
