package com.konbip.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

	/**
	 * 판매 중인 상품 검색. 조건이 null 이면 그 조건은 무시한다.
	 * namePattern 은 '%키워드%' 형태로 넘긴다. (ProductService.likePattern)
	 */
	@Query("""
			SELECT p FROM Product p
			WHERE p.active = true
			  AND (:namePattern IS NULL OR p.name LIKE :namePattern ESCAPE '!')
			  AND (:brand IS NULL OR p.brand = :brand)
			  AND (:category IS NULL OR p.category = :category)
			""")
	Page<Product> search(@Param("namePattern") String namePattern, @Param("brand") String brand,
			@Param("category") String category, Pageable pageable);

	@Query("SELECT DISTINCT p.brand FROM Product p WHERE p.active = true AND p.brand IS NOT NULL ORDER BY p.brand")
	List<String> findBrands();

	@Query("""
			SELECT DISTINCT p.category FROM Product p
			WHERE p.active = true AND p.category IS NOT NULL
			  AND (:brand IS NULL OR p.brand = :brand)
			ORDER BY p.category
			""")
	List<String> findCategories(@Param("brand") String brand);
}
