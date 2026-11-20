package com.konbip.recipe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.konbip.global.response.PageResponse;
import com.konbip.product.ProductService;
import com.konbip.ranking.RankingItemResponse;
import com.konbip.ranking.RankingType;
import com.konbip.recipe.dto.RecipeSummaryResponse;

import lombok.RequiredArgsConstructor;

/**
 * 꿀조합 카드 목록 조회 (검색, 정렬, 랭킹, 마이페이지).
 * <p>
 * 카드마다 좋아요·즐겨찾기·댓글·리뷰 수, 평균 별점, 조회수, 칼로리 합계가 필요하고
 * 그 값으로 정렬도 해야 해서, JPA 대신 집계 서브쿼리가 들어간 SQL 한 번으로 가져온다.
 * MySQL 과 H2(MySQL 모드, 테스트용)에서 모두 동작하는 표준 SQL 만 쓴다.
 */
@Repository
@RequiredArgsConstructor
public class RecipeQueryRepository {

	private static final String SUMMARY_SELECT = """
			SELECT r.recipe_id, r.title, r.brand, r.image_type, r.created_at,
			       r.user_id AS author_id, u.nickname AS author_nickname,
			       (SELECT SUM(ri.calories * ri.quantity) FROM recipe_items ri
			         WHERE ri.recipe_id = r.recipe_id) AS total_calories,
			       (SELECT COUNT(*) FROM interactions i
			         WHERE i.recipe_id = r.recipe_id AND i.type = 'LIKE') AS like_count,
			       (SELECT COUNT(*) FROM interactions i
			         WHERE i.recipe_id = r.recipe_id AND i.type = 'BOOKMARK') AS bookmark_count,
			       (SELECT COUNT(*) FROM comments c
			         WHERE c.recipe_id = r.recipe_id AND c.type = 'COMMENT') AS comment_count,
			       (SELECT COUNT(*) FROM comments c
			         WHERE c.recipe_id = r.recipe_id AND c.type = 'REVIEW') AS review_count,
			       (SELECT AVG(c.rating * 1.0) FROM comments c
			         WHERE c.recipe_id = r.recipe_id AND c.type = 'REVIEW') AS average_rating,
			       (SELECT COUNT(*) FROM recipe_views v
			         WHERE v.recipe_id = r.recipe_id AND v.type = 'VIEW') AS view_count
			""";

	private static final String FROM = " FROM recipes r JOIN users u ON u.user_id = r.user_id WHERE 1 = 1";

	private final NamedParameterJdbcTemplate jdbc;

	/** RECIPE-001 꿀조합 목록 및 검색 */
	public PageResponse<RecipeSummaryResponse> search(RecipeSearchCondition condition, RecipeSort sort,
			Pageable pageable) {
		Spec spec = new Spec();
		String keyword = ProductService.likePattern(condition.keyword());
		if (keyword != null) {
			spec.where.append("""
					 AND (r.title LIKE :keyword ESCAPE '!'
					      OR r.description LIKE :keyword ESCAPE '!'
					      OR EXISTS (SELECT 1 FROM recipe_tags kt
					                  WHERE kt.recipe_id = r.recipe_id AND kt.name LIKE :keyword ESCAPE '!')
					      OR EXISTS (SELECT 1 FROM recipe_items ki JOIN products kp ON kp.product_id = ki.product_id
					                  WHERE ki.recipe_id = r.recipe_id AND kp.name LIKE :keyword ESCAPE '!'))
					""");
			spec.params.addValue("keyword", keyword);
		}
		String brand = ProductService.blankToNull(condition.brand());
		if (brand != null) {
			spec.where.append("""
					 AND (r.brand = :brand
					      OR EXISTS (SELECT 1 FROM recipe_items bi JOIN products bp ON bp.product_id = bi.product_id
					                  WHERE bi.recipe_id = r.recipe_id AND bp.brand = :brand))
					""");
			spec.params.addValue("brand", brand);
		}
		String category = ProductService.blankToNull(condition.category());
		if (category != null) {
			spec.where.append("""
					 AND EXISTS (SELECT 1 FROM recipe_items ci JOIN products cp ON cp.product_id = ci.product_id
					              WHERE ci.recipe_id = r.recipe_id AND cp.category = :category)
					""");
			spec.params.addValue("category", category);
		}
		String tag = RecipeService.normalizeTag(condition.tag());
		if (tag != null) {
			spec.where.append(" AND EXISTS (SELECT 1 FROM recipe_tags tt WHERE tt.recipe_id = r.recipe_id AND tt.name = :tag)");
			spec.params.addValue("tag", tag);
		}
		if (condition.minRating() != null) {
			spec.outerWhere.append(" AND average_rating >= :minRating");
			spec.params.addValue("minRating", condition.minRating());
		}
		spec.orderBy = sort.orderBy();
		return toSummaries(fetch(spec, pageable));
	}

	/** 홈 화면용 상위 N개 */
	public List<RecipeSummaryResponse> findTop(RecipeSort sort, int limit) {
		Spec spec = new Spec();
		spec.orderBy = sort.orderBy();
		return toSummaries(fetch(spec, PageRequest.of(0, limit))).content();
	}

	/** 카드 1개 (상세 화면의 집계 값) */
	public Optional<RecipeSummaryResponse> findSummary(Long recipeId) {
		Spec spec = new Spec();
		spec.where.append(" AND r.recipe_id = :recipeId");
		spec.params.addValue("recipeId", recipeId);
		return toSummaries(fetch(spec, PageRequest.of(0, 1))).content().stream().findFirst();
	}

	/** MY-001 내가 작성한 꿀조합 */
	public PageResponse<RecipeSummaryResponse> findByAuthor(Long authorId, Pageable pageable) {
		Spec spec = new Spec();
		spec.where.append(" AND r.user_id = :authorId");
		spec.params.addValue("authorId", authorId);
		return toSummaries(fetch(spec, pageable));
	}

	/** MY-002, MY-003 좋아요/즐겨찾기한 꿀조합. 최근에 누른 순서 */
	public PageResponse<RecipeSummaryResponse> findByInteraction(Long userId, String interactionType,
			Pageable pageable) {
		Spec spec = new Spec();
		spec.where.append("""
				 AND EXISTS (SELECT 1 FROM interactions mi
				              WHERE mi.recipe_id = r.recipe_id AND mi.user_id = :viewerId AND mi.type = :interactionType)
				""");
		spec.extraColumns.append("""
				, (SELECT mi2.created_at FROM interactions mi2
				    WHERE mi2.recipe_id = r.recipe_id AND mi2.user_id = :viewerId
				      AND mi2.type = :interactionType) AS interacted_at
				""");
		spec.params.addValue("viewerId", userId).addValue("interactionType", interactionType);
		spec.orderBy = "interacted_at DESC, recipe_id DESC";
		return toSummaries(fetch(spec, pageable));
	}

	/**
	 * RANKING-001 기간별 랭킹.
	 *
	 * @param since          집계 시작 시각 (null 이면 전체 기간)
	 * @param minReviewCount 별점 랭킹에 들어가기 위한 기간 내 최소 리뷰 수
	 */
	public PageResponse<RankingItemResponse> ranking(RankingType type, LocalDateTime since, int minReviewCount,
			Pageable pageable) {
		Spec spec = new Spec();
		spec.withScore = true;
		String period = since == null ? "" : " AND x.created_at >= :since";
		if (since != null) {
			spec.params.addValue("since", since);
		}
		switch (type) {
			case VIEWS, BOOKMARKS -> {
				String activity = type == RankingType.VIEWS ? "VIEW" : "BOOKMARK";
				spec.extraColumns.append(", (SELECT COUNT(*) FROM recipe_views x WHERE x.recipe_id = r.recipe_id AND x.type = '")
						.append(activity).append("'").append(period).append(") AS score");
				spec.outerWhere.append(" AND score > 0");
			}
			case RATING -> {
				spec.extraColumns
						.append(", (SELECT AVG(x.rating * 1.0) FROM comments x WHERE x.recipe_id = r.recipe_id AND x.type = 'REVIEW'")
						.append(period).append(") AS score")
						.append(", (SELECT COUNT(*) FROM comments x WHERE x.recipe_id = r.recipe_id AND x.type = 'REVIEW'")
						.append(period).append(") AS score_count");
				spec.outerWhere.append(" AND score_count >= :minReviewCount AND score_count > 0");
				spec.params.addValue("minReviewCount", minReviewCount);
			}
		}
		spec.orderBy = "score DESC, like_count DESC, recipe_id DESC";

		PageResponse<Row> rows = fetch(spec, pageable);
		Map<Long, List<String>> tags = tagsOf(rows.content());
		List<RankingItemResponse> items = new ArrayList<>();
		long offset = pageable.getOffset();
		for (int i = 0; i < rows.content().size(); i++) {
			Row row = rows.content().get(i);
			Number score = type == RankingType.RATING ? round(row.score(), 2) : Long.valueOf(row.score().longValue());
			items.add(new RankingItemResponse((int) offset + i + 1, score, row.toResponse(tags)));
		}
		return new PageResponse<>(items, rows.page(), rows.size(), rows.totalElements(), rows.totalPages());
	}

	// ----------------------------------------------------------------------

	private PageResponse<Row> fetch(Spec spec, Pageable pageable) {
		String inner = SUMMARY_SELECT + spec.extraColumns + FROM + spec.where;
		String outerWhere = spec.outerWhere.toString();
		String countSql = outerWhere.isEmpty()
				? "SELECT COUNT(*)" + FROM + spec.where
				: "SELECT COUNT(*) FROM (" + inner + ") t WHERE 1 = 1" + outerWhere;
		Long total = jdbc.queryForObject(countSql, spec.params, Long.class);
		long totalElements = total == null ? 0 : total;
		int page = pageable.getPageNumber();
		int size = pageable.getPageSize();
		if (totalElements == 0 || pageable.getOffset() >= totalElements) {
			return PageResponse.of(List.of(), page, size, totalElements);
		}
		spec.params.addValue("limit", size).addValue("offset", pageable.getOffset());
		String sql = "SELECT * FROM (" + inner + ") t WHERE 1 = 1" + outerWhere
				+ " ORDER BY " + spec.orderBy + " LIMIT :limit OFFSET :offset";
		List<Row> rows = jdbc.query(sql, spec.params, rowMapper(spec.withScore));
		return PageResponse.of(rows, page, size, totalElements);
	}

	private PageResponse<RecipeSummaryResponse> toSummaries(PageResponse<Row> rows) {
		Map<Long, List<String>> tags = tagsOf(rows.content());
		List<RecipeSummaryResponse> content = rows.content().stream().map(row -> row.toResponse(tags)).toList();
		return new PageResponse<>(content, rows.page(), rows.size(), rows.totalElements(), rows.totalPages());
	}

	private Map<Long, List<String>> tagsOf(List<Row> rows) {
		if (rows.isEmpty()) {
			return Map.of();
		}
		List<Long> ids = rows.stream().map(Row::recipeId).toList();
		Map<Long, List<String>> tags = new HashMap<>();
		jdbc.query("SELECT recipe_id, name FROM recipe_tags WHERE recipe_id IN (:ids) ORDER BY tag_id",
				new MapSqlParameterSource("ids", ids),
				rs -> {
					tags.computeIfAbsent(rs.getLong("recipe_id"), key -> new ArrayList<>()).add(rs.getString("name"));
				});
		return tags;
	}

	private static RowMapper<Row> rowMapper(boolean withScore) {
		return (rs, rowNum) -> new Row(
				rs.getLong("recipe_id"),
				rs.getString("title"),
				rs.getString("brand"),
				rs.getString("image_type") != null,
				rs.getObject("created_at", LocalDateTime.class),
				rs.getLong("author_id"),
				rs.getString("author_nickname"),
				rs.getBigDecimal("total_calories"),
				rs.getLong("like_count"),
				rs.getLong("bookmark_count"),
				rs.getLong("comment_count"),
				rs.getLong("review_count"),
				round(rs.getBigDecimal("average_rating"), 1),
				rs.getLong("view_count"),
				withScore ? decimal(rs, "score") : null);
	}

	private static BigDecimal decimal(ResultSet rs, String column) throws SQLException {
		BigDecimal value = rs.getBigDecimal(column);
		return value == null ? BigDecimal.ZERO : value;
	}

	private static Double round(BigDecimal value, int scale) {
		return value == null ? null : value.setScale(scale, RoundingMode.HALF_UP).doubleValue();
	}

	private static final class Spec {
		private final StringBuilder where = new StringBuilder();
		private final StringBuilder extraColumns = new StringBuilder();
		private final StringBuilder outerWhere = new StringBuilder();
		private final MapSqlParameterSource params = new MapSqlParameterSource();
		private String orderBy = RecipeSort.LATEST.orderBy();
		private boolean withScore;
	}

	private record Row(
			long recipeId,
			String title,
			String brand,
			boolean hasImage,
			LocalDateTime createdAt,
			long authorId,
			String author,
			BigDecimal totalCalories,
			long likeCount,
			long bookmarkCount,
			long commentCount,
			long reviewCount,
			Double averageRating,
			long viewCount,
			BigDecimal score
	) {

		RecipeSummaryResponse toResponse(Map<Long, List<String>> tags) {
			return new RecipeSummaryResponse(
					recipeId,
					title,
					brand,
					hasImage ? RecipeSummaryResponse.imageUrl(recipeId) : null,
					authorId,
					author,
					tags.getOrDefault(recipeId, List.of()),
					totalCalories,
					likeCount,
					bookmarkCount,
					commentCount,
					reviewCount,
					averageRating,
					viewCount,
					createdAt);
		}
	}
}
