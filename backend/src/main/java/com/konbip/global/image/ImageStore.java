package com.konbip.global.image;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * recipes.image_data, users.profile_image_data (LONGBLOB) 읽기/쓰기.
 * <p>
 * 엔티티에 byte[] 로 매핑하면 목록·수정처럼 이미지가 필요 없는 곳에서도 BLOB 을 매번 읽게 되므로,
 * 이미지 바이트는 JPA 엔티티에서 빼고 여기서 SQL 로만 다룬다.
 * (같은 트랜잭션 안에서 JPA 와 같은 DB 커넥션을 쓴다.)
 */
@Repository
@RequiredArgsConstructor
public class ImageStore {

	private final JdbcTemplate jdbcTemplate;

	public void saveRecipeImage(Long recipeId, ImageFile image) {
		jdbcTemplate.update("UPDATE recipes SET image_data = ?, image_type = ? WHERE recipe_id = ?",
				image.data(), image.contentType(), recipeId);
	}

	public void deleteRecipeImage(Long recipeId) {
		jdbcTemplate.update("UPDATE recipes SET image_data = NULL, image_type = NULL WHERE recipe_id = ?", recipeId);
	}

	public Optional<StoredImage> findRecipeImage(Long recipeId) {
		return findOne("SELECT image_data, image_type FROM recipes WHERE recipe_id = ? AND image_data IS NOT NULL",
				recipeId);
	}

	public void saveProfileImage(Long userId, ImageFile image) {
		jdbcTemplate.update("UPDATE users SET profile_image_data = ?, profile_image_type = ? WHERE user_id = ?",
				image.data(), image.contentType(), userId);
	}

	public void deleteProfileImage(Long userId) {
		jdbcTemplate.update("UPDATE users SET profile_image_data = NULL, profile_image_type = NULL WHERE user_id = ?",
				userId);
	}

	public Optional<StoredImage> findProfileImage(Long userId) {
		return findOne("SELECT profile_image_data, profile_image_type FROM users "
				+ "WHERE user_id = ? AND profile_image_data IS NOT NULL", userId);
	}

	private Optional<StoredImage> findOne(String sql, Long id) {
		List<StoredImage> rows = jdbcTemplate.query(sql,
				(rs, rowNum) -> new StoredImage(rs.getBytes(1), rs.getString(2)), id);
		return rows.stream().findFirst();
	}
}
