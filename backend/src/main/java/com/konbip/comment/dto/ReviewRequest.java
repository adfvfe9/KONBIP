package com.konbip.comment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** FR-SOC-05 리뷰: 별점 1~5 정수, 내용 최대 500자 (내용은 비워도 된다) */
public record ReviewRequest(
		@NotNull(message = "별점을 선택해 주세요.")
		@Min(value = 1, message = "별점은 1~5점입니다.")
		@Max(value = 5, message = "별점은 1~5점입니다.")
		Integer rating,

		@Size(max = 500, message = "리뷰는 500자 이하여야 합니다.")
		String content
) {
}
