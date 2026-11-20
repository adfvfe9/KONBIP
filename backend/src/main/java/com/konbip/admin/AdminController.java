package com.konbip.admin;

import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.konbip.comment.CommentService;
import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;
import com.konbip.global.response.ApiResponse;
import com.konbip.global.response.PageResponse;
import com.konbip.global.response.Pages;
import com.konbip.product.sync.ProductSyncJob;
import com.konbip.product.sync.SyncResult;
import com.konbip.recipe.RecipeService;
import com.konbip.report.ReportService;
import com.konbip.report.ReportStatus;
import com.konbip.report.dto.ReportResponse;
import com.konbip.report.dto.ReportStatusRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 API. role=ADMIN 회원만 호출할 수 있다. (SecurityConfig: /api/admin/**)
 * 관리자 계정은 DB 에서 직접 지정한다: UPDATE users SET role = 'ADMIN' WHERE email = '...';
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final ReportService reportService;
	private final RecipeService recipeService;
	private final CommentService commentService;
	private final ProductSyncJob productSyncJob;

	/** ADMIN-001 신고 목록. status=PENDING|RESOLVED|REJECTED (생략하면 전체) */
	@GetMapping("/reports")
	public ApiResponse<PageResponse<ReportResponse>> reports(
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		Sort newestFirst = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
		return ApiResponse.ok(reportService.getReports(parseStatus(status), Pages.of(page, size, newestFirst)));
	}

	/** ADMIN-002 신고 처리 */
	@PatchMapping("/reports/{reportId}")
	public ApiResponse<ReportResponse> handleReport(@PathVariable Long reportId,
			@Valid @RequestBody ReportStatusRequest request) {
		return ApiResponse.ok("신고 상태가 변경되었습니다.", reportService.changeStatus(reportId, request.status()));
	}

	/** ADMIN-003 꿀조합 삭제 */
	@DeleteMapping("/recipes/{recipeId}")
	public ApiResponse<Void> deleteRecipe(@PathVariable Long recipeId) {
		recipeService.deleteByAdmin(recipeId);
		return ApiResponse.ok("꿀조합이 삭제되었습니다.", null);
	}

	/** ADMIN-004 댓글(리뷰 포함) 삭제 */
	@DeleteMapping("/comments/{commentId}")
	public ApiResponse<Void> deleteComment(@PathVariable Long commentId) {
		commentService.deleteByAdmin(commentId);
		return ApiResponse.ok("댓글이 삭제되었습니다.", null);
	}

	/** ADMIN-005 상품 데이터 수동 동기화 */
	@PostMapping("/products/sync")
	public ApiResponse<SyncResult> syncProducts() {
		return ApiResponse.ok("상품 동기화가 완료되었습니다.", productSyncJob.runNow());
	}

	private static ReportStatus parseStatus(String status) {
		if (status == null || status.isBlank()) {
			return null;
		}
		try {
			return ReportStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "status 는 PENDING, RESOLVED, REJECTED 중 하나여야 합니다.");
		}
	}
}
