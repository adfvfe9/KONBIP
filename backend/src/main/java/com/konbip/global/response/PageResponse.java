package com.konbip.global.response;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/**
 * API 명세서 1.4 페이지네이션 응답 형식.
 */
public record PageResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {

	public static <T> PageResponse<T> of(Page<T> page) {
		return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
				page.getTotalElements(), page.getTotalPages());
	}

	public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
		return of(page.map(mapper));
	}

	public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
		int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
		return new PageResponse<>(content, page, size, totalElements, totalPages);
	}
}
