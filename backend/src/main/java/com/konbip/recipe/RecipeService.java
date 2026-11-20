package com.konbip.recipe;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.konbip.comment.CommentRepository;
import com.konbip.comment.CommentType;
import com.konbip.global.error.BusinessException;
import com.konbip.global.error.ErrorCode;
import com.konbip.global.image.ImageFile;
import com.konbip.global.image.ImageStore;
import com.konbip.global.image.StoredImage;
import com.konbip.global.response.PageResponse;
import com.konbip.interaction.ActivityType;
import com.konbip.interaction.InteractionRepository;
import com.konbip.interaction.InteractionType;
import com.konbip.interaction.RecipeActivity;
import com.konbip.interaction.RecipeActivityRepository;
import com.konbip.product.Product;
import com.konbip.product.ProductRepository;
import com.konbip.recipe.dto.RecipeDetailResponse;
import com.konbip.recipe.dto.RecipeRequest;
import com.konbip.recipe.dto.RecipeSummaryResponse;
import com.konbip.user.User;
import com.konbip.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeService {

	private static final int MAX_QUANTITY_PER_PRODUCT = 10;
	private static final int MAX_TAG_LENGTH = 30;

	private final RecipeRepository recipeRepository;
	private final RecipeQueryRepository recipeQueryRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final InteractionRepository interactionRepository;
	private final RecipeActivityRepository activityRepository;
	private final CommentRepository commentRepository;
	private final ImageStore imageStore;

	/** RECIPE-001 목록·검색·정렬 */
	public PageResponse<RecipeSummaryResponse> search(RecipeSearchCondition condition, RecipeSort sort,
			Pageable pageable) {
		return recipeQueryRepository.search(condition, sort, pageable);
	}

	/** RECIPE-002 상세. 조회 이력도 함께 남긴다. (FR-RCP-09) */
	@Transactional
	public RecipeDetailResponse getDetail(Long recipeId, Long viewerId) {
		Recipe recipe = recipeRepository.findWithUserById(recipeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.RECIPE_NOT_FOUND));
		recordView(recipeId, viewerId);
		RecipeSummaryResponse summary = recipeQueryRepository.findSummary(recipeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.RECIPE_NOT_FOUND));

		RecipeDetailResponse.ViewerState viewer = viewerId == null
				? new RecipeDetailResponse.ViewerState(false, false, false, false)
				: new RecipeDetailResponse.ViewerState(
						interactionRepository.existsByUserIdAndRecipeIdAndType(viewerId, recipeId, InteractionType.LIKE),
						interactionRepository.existsByUserIdAndRecipeIdAndType(viewerId, recipeId, InteractionType.BOOKMARK),
						commentRepository.existsByUserAndRecipe(viewerId, recipeId, CommentType.REVIEW),
						recipe.isWrittenBy(viewerId));
		return RecipeDetailResponse.of(recipe, summary, viewer);
	}

	/** RECIPE-003 등록 */
	@Transactional
	public Long create(Long userId, RecipeRequest request, ImageFile image) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		Recipe recipe = Recipe.create(user, request.title().trim(), trimToNull(request.description()),
				trimToNull(request.brand()));
		recipe.replaceItems(toDrafts(request.items(), Set.of()));
		recipe.replaceSteps(normalizeSteps(request.steps()));
		recipe.replaceTags(normalizeTags(request.tags()));
		recipeRepository.save(recipe);
		if (image != null) {
			imageStore.saveRecipeImage(recipe.getId(), image);
		}
		return recipe.getId();
	}

	/**
	 * RECIPE-004 수정 (작성자만). 등록과 같은 형식으로 전체 내용을 다시 보낸다.
	 * image 를 보내면 교체, removeImage=true 면 삭제, 둘 다 없으면 기존 이미지 유지.
	 */
	@Transactional
	public void update(Long userId, Long recipeId, RecipeRequest request, ImageFile image, boolean removeImage) {
		Recipe recipe = findOwned(userId, recipeId);
		Set<Long> keptProductIds = recipe.getItems().stream()
				.map(item -> item.getProduct().getId())
				.collect(Collectors.toSet());
		recipe.updateInfo(request.title().trim(), trimToNull(request.description()), trimToNull(request.brand()));
		recipe.replaceItems(toDrafts(request.items(), keptProductIds));
		recipe.replaceSteps(normalizeSteps(request.steps()));
		recipe.replaceTags(normalizeTags(request.tags()));
		if (image != null) {
			imageStore.saveRecipeImage(recipeId, image);
		} else if (removeImage) {
			imageStore.deleteRecipeImage(recipeId);
		}
	}

	/** RECIPE-005 삭제 (작성자만) */
	@Transactional
	public void delete(Long userId, Long recipeId) {
		recipeRepository.delete(findOwned(userId, recipeId));
	}

	/** ADMIN-003 관리자 삭제 */
	@Transactional
	public void deleteByAdmin(Long recipeId) {
		Recipe recipe = recipeRepository.findById(recipeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.RECIPE_NOT_FOUND));
		recipeRepository.delete(recipe);
	}

	/** IMAGE-001 */
	public StoredImage getImage(Long recipeId) {
		return imageStore.findRecipeImage(recipeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));
	}

	// ----------------------------------------------------------------------

	private Recipe findOwned(Long userId, Long recipeId) {
		Recipe recipe = recipeRepository.findById(recipeId)
				.orElseThrow(() -> new BusinessException(ErrorCode.RECIPE_NOT_FOUND));
		if (!recipe.isWrittenBy(userId)) {
			throw new BusinessException(ErrorCode.NOT_RECIPE_AUTHOR);
		}
		return recipe;
	}

	/** 로그인 회원은 같은 꿀조합을 하루에 한 번만 조회수로 센다. */
	private void recordView(Long recipeId, Long viewerId) {
		if (viewerId != null && activityRepository.existsByRecipeIdAndUserIdAndTypeAndCreatedAtGreaterThanEqual(
				recipeId, viewerId, ActivityType.VIEW, LocalDate.now().atStartOfDay())) {
			return;
		}
		activityRepository.save(RecipeActivity.of(recipeId, viewerId, ActivityType.VIEW));
	}

	/**
	 * 요청의 구성 상품을 검증한다.
	 * <ul>
	 *   <li>같은 상품이 여러 번 오면 수량을 합친다 (FR-CMB-01). 합친 수량은 10개 이하.</li>
	 *   <li>단종 상품은 새로 담을 수 없다. 수정 시 원래 들어 있던 단종 상품은 유지할 수 있다.</li>
	 *   <li>가격: 상품 가격(API)이 있으면 그 값, 없으면 사용자가 입력한 customPrice.</li>
	 * </ul>
	 */
	private List<RecipeItem.Draft> toDrafts(List<RecipeRequest.Item> items, Set<Long> keptProductIds) {
		Map<Long, Integer> quantities = new LinkedHashMap<>();
		Map<Long, BigDecimal> customPrices = new HashMap<>();
		for (RecipeRequest.Item item : items) {
			quantities.merge(item.productId(), item.quantity(), Integer::sum);
			if (item.customPrice() != null) {
				customPrices.put(item.productId(), item.customPrice());
			}
		}
		if (quantities.values().stream().anyMatch(quantity -> quantity > MAX_QUANTITY_PER_PRODUCT)) {
			throw new BusinessException(ErrorCode.TOO_MANY_OF_PRODUCT);
		}

		Map<Long, Product> products = productRepository.findAllById(quantities.keySet()).stream()
				.collect(Collectors.toMap(Product::getId, Function.identity()));
		List<RecipeItem.Draft> drafts = new ArrayList<>();
		quantities.forEach((productId, quantity) -> {
			Product product = products.get(productId);
			if (product == null) {
				throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "상품을 찾을 수 없습니다. (productId=" + productId + ")");
			}
			if (!product.isActive() && !keptProductIds.contains(productId)) {
				throw new BusinessException(ErrorCode.INACTIVE_PRODUCT,
						ErrorCode.INACTIVE_PRODUCT.getMessage() + " (" + product.getName() + ")");
			}
			BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : customPrices.get(productId);
			drafts.add(new RecipeItem.Draft(product, quantity, unitPrice));
		});
		return drafts;
	}

	/** '#매콤 ' → '매콤'. 공백 제거, 중복 제거, 순서 유지 */
	static List<String> normalizeTags(List<String> tags) {
		if (tags == null) {
			return List.of();
		}
		Set<String> result = new LinkedHashSet<>();
		for (String tag : tags) {
			String normalized = normalizeTag(tag);
			if (normalized != null) {
				result.add(normalized);
			}
		}
		return List.copyOf(result);
	}

	static String normalizeTag(String tag) {
		if (tag == null) {
			return null;
		}
		String value = tag.strip();
		while (value.startsWith("#")) {
			value = value.substring(1);
		}
		value = value.replaceAll("\\s+", "");
		if (value.isEmpty()) {
			return null;
		}
		return value.length() > MAX_TAG_LENGTH ? value.substring(0, MAX_TAG_LENGTH) : value;
	}

	private static List<String> normalizeSteps(List<String> steps) {
		if (steps == null) {
			return List.of();
		}
		return steps.stream().map(String::strip).filter(step -> !step.isEmpty()).toList();
	}

	private static String trimToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
