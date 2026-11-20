package com.konbip.report.dto;

import com.konbip.report.ReportStatus;

import jakarta.validation.constraints.NotNull;

/** ADMIN-002 신고 처리: RESOLVED 또는 REJECTED */
public record ReportStatusRequest(@NotNull ReportStatus status) {
}
