package com.konbip.report;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.konbip.comment.CommentRepository;
import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;
import com.konbip.global.response.PageResponse;
import com.konbip.recipe.RecipeRepository;
import com.konbip.report.dto.ReportRequest;
import com.konbip.report.dto.ReportResponse;

import lombok.RequiredArgsConstructor;

/** FR-SOC-08 신고. 같은 회원이 같은 대상을 두 번 신고할 수 없다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

	private final ReportRepository reportRepository;
	private final RecipeRepository recipeRepository;
	private final CommentRepository commentRepository;

	@Transactional
	public ReportResponse report(Long userId, ReportRequest request) {
		boolean targetExists = switch (request.targetType()) {
			case RECIPE -> recipeRepository.existsById(request.targetId());
			case COMMENT -> commentRepository.existsById(request.targetId());
		};
		if (!targetExists) {
			throw new BusinessException(ErrorCode.REPORT_TARGET_NOT_FOUND);
		}
		if (reportRepository.existsByUserIdAndTargetTypeAndTargetId(userId, request.targetType(), request.targetId())) {
			throw new BusinessException(ErrorCode.DUPLICATE_REPORT);
		}
		Report report = reportRepository.save(
				Report.of(userId, request.targetType(), request.targetId(), request.reason().trim()));
		return ReportResponse.from(report);
	}

	/** ADMIN-001 신고 목록. status 가 null 이면 전체 */
	public PageResponse<ReportResponse> getReports(ReportStatus status, Pageable pageable) {
		return PageResponse.of(
				status == null ? reportRepository.findAll(pageable) : reportRepository.findByStatus(status, pageable),
				ReportResponse::from);
	}

	/** ADMIN-002 신고 처리 상태 변경 */
	@Transactional
	public ReportResponse changeStatus(Long reportId, ReportStatus status) {
		Report report = reportRepository.findById(reportId)
				.orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));
		report.changeStatus(status);
		return ReportResponse.from(report);
	}
}
