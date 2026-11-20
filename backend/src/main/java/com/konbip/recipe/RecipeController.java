package com.konbip.recipe;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.konbip.global.image.ImageFile;
import com.konbip.global.response.ApiResponse;
import com.konbip.global.response.PageResponse;
import com.konbip.global.response.Pages;
import com.konbip.global.security.LoginUser;
import com.konbip.global.web.JsonPartParser;
import com.konbip.recipe.dto.RecipeDetailResponse;
import com.konbip.recipe.dto.RecipeIdResponse;
import com.konbip.recipe.dto.RecipeRequest;
import com.konbip.recipe.dto.RecipeSummaryResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

	private final RecipeService recipeService;
	private final JsonPartParser jsonPartParser;

	/**
	 * RECIPE-001 꿀조합 목록 및 검색.
	 * sort: latest(기본), popular(좋아요), rating(별점), views(조회수), bookmarks(즐겨찾기)
	 */
	@GetMapping
	public ApiResponse<PageResponse<RecipeSummaryResponse>> search(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String brand,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) String tag,
			@RequestParam(required = false) Double minRating,
			@RequestParam(required = false) String sort,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		RecipeSearchCondition condition = new RecipeSearchCondition(keyword, brand, category, tag, minRating);
		return ApiResponse.ok("꿀조합 조회 성공",
				recipeService.search(condition, RecipeSort.from(sort), Pages.of(page, size)));
	}

	/** RECIPE-002 꿀조합 상세. 로그인했으면 isLiked 등 내 상태도 채운다. */
	@GetMapping("/{recipeId}")
	public ApiResponse<RecipeDetailResponse> detail(@LoginUser(required = false) Long userId,
			@PathVariable Long recipeId) {
		return ApiResponse.ok("꿀조합 조회 성공", recipeService.getDetail(recipeId, userId));
	}

	/**
	 * RECIPE-003 꿀조합 등록 (multipart/form-data).
	 * <ul>
	 *   <li>recipe: RecipeRequest JSON 문자열 (필수)</li>
	 *   <li>image: jpg/png 5MB 이하 (선택)</li>
	 * </ul>
	 */
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<RecipeIdResponse> create(
			@LoginUser Long userId,
			@RequestPart("recipe") String recipe,
			@RequestPart(value = "image", required = false) MultipartFile image) {
		RecipeRequest request = jsonPartParser.parse(recipe, RecipeRequest.class);
		Long recipeId = recipeService.create(userId, request, ImageFile.fromNullable(image));
		return ApiResponse.ok("꿀조합이 등록되었습니다.", new RecipeIdResponse(recipeId));
	}

	/** RECIPE-003 꿀조합 등록 (이미지 없이 JSON 본문으로) */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<RecipeIdResponse> createJson(@LoginUser Long userId,
			@Valid @RequestBody RecipeRequest request) {
		return ApiResponse.ok("꿀조합이 등록되었습니다.", new RecipeIdResponse(recipeService.create(userId, request, null)));
	}

	/**
	 * RECIPE-004 꿀조합 수정 (multipart/form-data, 작성자만).
	 * recipe, image 는 등록과 같고, removeImage=true 를 보내면 기존 이미지를 지운다.
	 */
	@PatchMapping(value = "/{recipeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<RecipeIdResponse> update(
			@LoginUser Long userId,
			@PathVariable Long recipeId,
			@RequestPart("recipe") String recipe,
			@RequestPart(value = "image", required = false) MultipartFile image,
			@RequestParam(defaultValue = "false") boolean removeImage) {
		RecipeRequest request = jsonPartParser.parse(recipe, RecipeRequest.class);
		recipeService.update(userId, recipeId, request, ImageFile.fromNullable(image), removeImage);
		return ApiResponse.ok("꿀조합이 수정되었습니다.", new RecipeIdResponse(recipeId));
	}

	/** RECIPE-004 꿀조합 수정 (JSON 본문, 이미지는 그대로 유지) */
	@PatchMapping(value = "/{recipeId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponse<RecipeIdResponse> updateJson(@LoginUser Long userId, @PathVariable Long recipeId,
			@Valid @RequestBody RecipeRequest request) {
		recipeService.update(userId, recipeId, request, null, false);
		return ApiResponse.ok("꿀조합이 수정되었습니다.", new RecipeIdResponse(recipeId));
	}

	/** RECIPE-005 꿀조합 삭제 (작성자만) */
	@DeleteMapping("/{recipeId}")
	public ApiResponse<Void> delete(@LoginUser Long userId, @PathVariable Long recipeId) {
		recipeService.delete(userId, recipeId);
		return ApiResponse.ok("꿀조합이 삭제되었습니다.", null);
	}

	/** IMAGE-001 꿀조합 이미지 (바이너리) */
	@GetMapping("/{recipeId}/image")
	public ResponseEntity<byte[]> image(@PathVariable Long recipeId) {
		return recipeService.getImage(recipeId).toResponse();
	}
}
