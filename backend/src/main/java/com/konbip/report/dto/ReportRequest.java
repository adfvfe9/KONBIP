package com.konbip.report.dto;

import com.konbip.report.ReportTargetType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * REPORT-001 신고. reason 은 프론트에서 고른 사유(욕설, 광고, 허위 정보, 기타 ...)를 그대로 보낸다.
 */
public record ReportRequest(
		@NotNull ReportTargetType targetType,
		@NotNull Long targetId,
		@NotBlank(message = "신고 사유를 입력해 주세요.")
		@Size(max = 500, message = "신고 사유는 500자 이하여야 합니다.")
		String reason
) {
}
