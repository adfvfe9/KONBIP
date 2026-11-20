package com.konbip.user;

import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.konbip.comment.CommentService;
import com.konbip.comment.dto.ReviewResponse;
import com.konbip.global.image.ImageFile;
import com.konbip.global.response.ApiResponse;
import com.konbip.global.response.PageResponse;
import com.konbip.global.response.Pages;
import com.konbip.global.security.LoginUser;
import com.konbip.interaction.InteractionType;
import com.konbip.recipe.RecipeQueryRepository;
import com.konbip.recipe.dto.RecipeSummaryResponse;
import com.konbip.user.dto.ChangePasswordRequest;
import com.konbip.user.dto.UpdateProfileRequest;
import com.konbip.user.dto.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 내 정보 / 마이페이지 / 계정 설정.
 */
@RestController
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final RecipeQueryRepository recipeQueryRepository;
	private final CommentService commentService;

	/** AUTH-007 내 정보 조회 */
	@GetMapping("/api/users/me")
	public ApiResponse<UserResponse> me(@LoginUser Long userId) {
		return ApiResponse.ok(userService.getMe(userId));
	}

	/** AUTH-008 내 정보 수정 (닉네임) */
	@PatchMapping("/api/users/me")
	public ApiResponse<UserResponse> updateProfile(@LoginUser Long userId,
			@Valid @RequestBody UpdateProfileRequest request) {
		return ApiResponse.ok("프로필이 수정되었습니다.", userService.updateProfile(userId, request));
	}

	/** 비밀번호 변경 (FR-AUT-06) */
	@PatchMapping("/api/users/me/password")
	public ApiResponse<Void> changePassword(@LoginUser Long userId,
			@Valid @RequestBody ChangePasswordRequest request) {
		userService.changePassword(userId, request);
		return ApiResponse.ok("비밀번호가 변경되었습니다.", null);
	}

	/** 회원 탈퇴 (FR-AUT-07) */
	@DeleteMapping("/api/users/me")
	public ApiResponse<Void> withdraw(@LoginUser Long userId) {
		userService.withdraw(userId);
		return ApiResponse.ok("회원 탈퇴가 완료되었습니다.", null);
	}

	/** 프로필 사진 변경 (multipart/form-data, image 파트) */
	@PutMapping(value = "/api/users/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<UserResponse> updateProfileImage(@LoginUser Long userId,
			@RequestPart("image") MultipartFile image) {
		return ApiResponse.ok("프로필 사진이 변경되었습니다.", userService.updateProfileImage(userId, ImageFile.from(image)));
	}

	/** 프로필 사진 삭제 */
	@DeleteMapping("/api/users/me/profile-image")
	public ApiResponse<UserResponse> deleteProfileImage(@LoginUser Long userId) {
		return ApiResponse.ok("프로필 사진이 삭제되었습니다.", userService.deleteProfileImage(userId));
	}

	/** 프로필 사진 (바이너리, 비회원도 조회 가능) */
	@GetMapping("/api/users/{userId}/profile-image")
	public ResponseEntity<byte[]> profileImage(@PathVariable Long userId) {
		return userService.getProfileImage(userId).toResponse();
	}

	/** MY-001 내가 작성한 꿀조합 (최신순) */
	@GetMapping("/api/users/me/recipes")
	public ApiResponse<PageResponse<RecipeSummaryResponse>> myRecipes(@LoginUser Long userId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return ApiResponse.ok(recipeQueryRepository.findByAuthor(userId, Pages.of(page, size)));
	}

	/** MY-002 좋아요한 꿀조합 (최근 누른 순) */
	@GetMapping("/api/users/me/likes")
	public ApiResponse<PageResponse<RecipeSummaryResponse>> myLikes(@LoginUser Long userId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return ApiResponse.ok(recipeQueryRepository.findByInteraction(userId, InteractionType.LIKE.name(),
				Pages.of(page, size)));
	}

	/** MY-003 즐겨찾기한 꿀조합 (최근 누른 순) - UI "저장한 조합 보기" */
	@GetMapping("/api/users/me/bookmarks")
	public ApiResponse<PageResponse<RecipeSummaryResponse>> myBookmarks(@LoginUser Long userId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return ApiResponse.ok(recipeQueryRepository.findByInteraction(userId, InteractionType.BOOKMARK.name(),
				Pages.of(page, size)));
	}

	/** MY-004 내가 작성한 리뷰 (최신순) */
	@GetMapping("/api/users/me/reviews")
	public ApiResponse<PageResponse<ReviewResponse>> myReviews(@LoginUser Long userId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		Sort newestFirst = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
		return ApiResponse.ok(commentService.getMyReviews(userId, Pages.of(page, size, newestFirst)));
	}
}
