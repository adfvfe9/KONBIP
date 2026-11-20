package com.konbip.dev;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.konbip.comment.CommentService;
import com.konbip.comment.dto.CommentRequest;
import com.konbip.comment.dto.ReviewRequest;
import com.konbip.interaction.InteractionService;
import com.konbip.product.Product;
import com.konbip.product.ProductRepository;
import com.konbip.product.sync.ExternalProduct;
import com.konbip.product.sync.ProductSyncService;
import com.konbip.recipe.RecipeService;
import com.konbip.recipe.dto.RecipeRequest;
import com.konbip.user.Role;
import com.konbip.user.User;
import com.konbip.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로컬 개발용 샘플 데이터. (application-local.yml 의 konbip.seed.enabled=true 일 때, DB 가 비어 있으면 1회)
 * <p>
 * 상품 가격·영양성분은 화면 개발용 근사치이며 실제 상품 정보가 아니다.
 * Tabecal 연동이 끝나면 실제 데이터로 바뀐다.
 * <p>
 * 샘플 계정 (비밀번호 모두 konbip1234!)
 * <ul>
 *   <li>demo@konbip.dev (콘삡)</li>
 *   <li>ramen@konbip.dev (라멘)</li>
 *   <li>dove@konbip.dev (비둘기)</li>
 *   <li>admin@konbip.dev (관리자, ADMIN)</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "konbip.seed.enabled", havingValue = "true")
public class LocalDataSeeder implements ApplicationRunner {

	public static final String SAMPLE_PASSWORD = "konbip1234!";

	private static final String SEVEN = "세븐일레븐";
	private static final String FAMILY = "패밀리마트";
	private static final String LAWSON = "로손";

	private static final List<ExternalProduct> SAMPLE_PRODUCTS = List.of(
			product("sample-7-001", "몽고탄멘 나카모토 컵라면", SEVEN, "라면", "321", "560", "66", "13", "27"),
			product("sample-7-002", "반숙 계란 (2입)", SEVEN, "반찬", "198", "140", "1", "12", "10"),
			product("sample-7-003", "돼지고기 장조림", SEVEN, "반찬", null, "210", "8", "15", "13"),
			product("sample-7-004", "체다 치즈 슬라이스", SEVEN, "유제품", "248", "60", "0.3", "3.8", "4.9"),
			product("sample-7-005", "금의 함박스테이크", SEVEN, "반찬", "429", "380", "18", "20", "25"),
			product("sample-7-006", "연어 주먹밥", SEVEN, "주먹밥", "178", "180", "38", "5", "1.2"),
			product("sample-fm-001", "패미치키", FAMILY, "치킨", "230", "250", "13", "13", "16"),
			product("sample-fm-002", "버터롤 (4입)", FAMILY, "빵", "158", "380", "50", "10", "15"),
			product("sample-fm-003", "양배추 샐러드", FAMILY, "샐러드", "138", "20", "4", "1", null),
			product("sample-fm-004", "수플레 푸딩", FAMILY, "디저트", "298", "290", "30", "5", "16"),
			product("sample-ls-001", "가라아게군 레귤러", LAWSON, "치킨", "238", "220", "8", "15", "14"),
			product("sample-ls-002", "참치마요 주먹밥", LAWSON, "주먹밥", "160", "230", "36", "5", "7"),
			product("sample-ls-003", "프리미엄 롤케이크", LAWSON, "디저트", "210", "220", "17", "3", "15"),
			product("sample-ls-004", "사과 모찌", LAWSON, "디저트", null, "150", "33", "2", "1"),
			product("sample-ls-005", "우롱차 500ml", LAWSON, "음료", "108", "0", "0", "0", "0"));

	private final ProductSyncService productSyncService;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RecipeService recipeService;
	private final InteractionService interactionService;
	private final CommentService commentService;

	@Override
	public void run(ApplicationArguments args) {
		if (userRepository.count() > 0 || productRepository.count() > 0) {
			log.info("Sample data skipped: database is not empty.");
			return;
		}
		productSyncService.apply(SAMPLE_PRODUCTS);
		Map<String, Long> p = productRepository.findAll().stream()
				.collect(Collectors.toMap(Product::getExternalId, Product::getId));

		String password = passwordEncoder.encode(SAMPLE_PASSWORD);
		Long demo = userRepository.save(User.emailUser("demo@konbip.dev", password, "콘삡")).getId();
		Long ramen = userRepository.save(User.emailUser("ramen@konbip.dev", password, "라멘")).getId();
		Long dove = userRepository.save(User.emailUser("dove@konbip.dev", password, "비둘기")).getId();
		User adminUser = User.emailUser("admin@konbip.dev", password, "관리자");
		adminUser.grantRole(Role.ADMIN);
		Long admin = userRepository.save(adminUser).getId();

		Long mongotanmen = recipeService.create(demo, new RecipeRequest(
				"몽고탄멘 조합",
				"몽고탄멘 조합은 일본 세븐일레븐의 매콤한 몽고탄멘에 돼지고기 장조림과 반숙란을 더한 인기 편의점 꿀조합이다.",
				SEVEN,
				List.of("매콤", "반숙란", "장조림"),
				List.of("준비한 몽고탄멘을 조리법대로 조리해 주세요.",
						"조리된 몽고탄멘 안에 반숙 계란, 돼지고기 장조림을 섞어 주세요."),
				List.of(item(p.get("sample-7-001"), 1, null),
						item(p.get("sample-7-002"), 1, null),
						item(p.get("sample-7-003"), 1, "350"))), null);

		Long famichiki = recipeService.create(ramen, new RecipeRequest(
				"패미치키 버거",
				"버터롤 사이에 패미치키와 양배추 샐러드를 끼워 만드는 간단 버거.",
				FAMILY,
				List.of("치킨", "버거", "간단"),
				List.of("버터롤을 반으로 가른다.", "양배추 샐러드와 패미치키를 넣는다."),
				List.of(item(p.get("sample-fm-001"), 1, null),
						item(p.get("sample-fm-002"), 1, null),
						item(p.get("sample-fm-003"), 1, null))), null);

		Long karaage = recipeService.create(dove, new RecipeRequest(
				"가라아게 오니기리 정식",
				"가라아게군과 참치마요 주먹밥, 우롱차로 든든한 한 끼.",
				LAWSON,
				List.of("든든", "점심"),
				List.of("가라아게군을 데운다.", "주먹밥과 함께 먹는다."),
				List.of(item(p.get("sample-ls-001"), 1, null),
						item(p.get("sample-ls-002"), 1, null),
						item(p.get("sample-ls-005"), 1, null))), null);

		Long hamburg = recipeService.create(ramen, new RecipeRequest(
				"치즈 함박 오니기리",
				"함박스테이크에 체다 치즈를 올리고 연어 주먹밥을 곁들인다.",
				SEVEN,
				List.of("치즈", "고기"),
				List.of("함박스테이크를 데운다.", "뜨거울 때 체다 치즈를 올린다."),
				List.of(item(p.get("sample-7-005"), 1, null),
						item(p.get("sample-7-004"), 2, null),
						item(p.get("sample-7-006"), 1, null))), null);

		Long dessert = recipeService.create(demo, new RecipeRequest(
				"디저트 플레이트",
				"롤케이크, 사과 모찌, 수플레 푸딩을 한 접시에.",
				LAWSON,
				List.of("디저트", "달달"),
				List.of("디저트를 한 접시에 담는다."),
				List.of(item(p.get("sample-ls-003"), 1, null),
						item(p.get("sample-ls-004"), 1, "180"),
						item(p.get("sample-fm-004"), 1, null))), null);

		for (Long user : List.of(ramen, dove, admin)) {
			interactionService.like(user, mongotanmen);
			interactionService.bookmark(user, mongotanmen);
		}
		interactionService.like(demo, famichiki);
		interactionService.like(dove, famichiki);
		interactionService.bookmark(demo, karaage);
		interactionService.like(demo, hamburg);

		commentService.createReview(ramen, mongotanmen, new ReviewRequest(5, "체다 치즈 추가하면 더 맛있어져요!"));
		commentService.createReview(dove, mongotanmen, new ReviewRequest(4, "맵지만 반숙란이 잘 어울려요."));
		commentService.createReview(admin, mongotanmen, new ReviewRequest(5, "편의점 조합 중 최고."));
		commentService.createReview(demo, famichiki, new ReviewRequest(4, "간단하고 맛있어요."));
		commentService.createReview(dove, dessert, new ReviewRequest(5, "달달함 폭발"));
		commentService.createComment(dove, mongotanmen, new CommentRequest("오늘 저녁은 이거다"));
		commentService.createComment(demo, karaage, new CommentRequest("우롱차 조합 좋네요"));

		log.info("Sample data seeded: {} products, 4 users, 5 recipes. (password: {})",
				SAMPLE_PRODUCTS.size(), SAMPLE_PASSWORD);
	}

	private static ExternalProduct product(String id, String name, String brand, String category, String price,
			String calories, String carbohydrate, String protein, String fat) {
		return new ExternalProduct(id, name, brand, category, decimal(price), decimal(calories),
				decimal(carbohydrate), decimal(protein), decimal(fat));
	}

	private static RecipeRequest.Item item(Long productId, int quantity, String customPrice) {
		return new RecipeRequest.Item(productId, quantity, decimal(customPrice));
	}

	private static BigDecimal decimal(String value) {
		return value == null ? null : new BigDecimal(value);
	}
}
