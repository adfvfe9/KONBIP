package com.konbip.report.dto;

import java.time.LocalDateTime;

import com.konbip.report.Report;

public record ReportResponse(
		Long reportId,
		Long reporterId,
		String targetType,
		Long targetId,
		String reason,
		String status,
		LocalDateTime createdAt
) {

	public static ReportResponse from(Report report) {
		return new ReportResponse(
				report.getId(),
				report.getUserId(),
				report.getTargetType().name(),
				report.getTargetId(),
				report.getReason(),
				report.getStatus().name(),
				report.getCreatedAt());
	}
}
