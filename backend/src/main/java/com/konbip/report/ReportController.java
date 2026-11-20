package com.konbip.report;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.konbip.global.response.ApiResponse;
import com.konbip.global.security.LoginUser;
import com.konbip.report.dto.ReportRequest;
import com.konbip.report.dto.ReportResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;

	/** REPORT-001 콘텐츠 신고 */
	@PostMapping("/api/reports")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ReportResponse> report(@LoginUser Long userId, @Valid @RequestBody ReportRequest request) {
		return ApiResponse.ok("신고가 접수되었습니다.", reportService.report(userId, request));
	}
}
