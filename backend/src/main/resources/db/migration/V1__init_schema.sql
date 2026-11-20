-- Konbip DB 명세서(Notion "8. DB 명세서") 원본 스키마
-- CREATE DATABASE / USE 구문은 Flyway가 연결된 DB에 적용하므로 제외했다.

-- 1. 회원
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),
    nickname VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    provider VARCHAR(30),
    provider_user_id VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_provider (provider, provider_user_id)
) ENGINE=InnoDB;

-- 2. 상품
CREATE TABLE products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(100),
    category VARCHAR(100),
    calories DECIMAL(10,2),
    carbohydrate DECIMAL(10,2),
    protein DECIMAL(10,2),
    fat DECIMAL(10,2),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_products_name (name),
    INDEX idx_products_brand (brand),
    INDEX idx_products_category (category)
) ENGINE=InnoDB;

-- 3. 꿀조합
CREATE TABLE recipes (
    recipe_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    image_data LONGBLOB,
    image_type VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_recipes_user (user_id),
    INDEX idx_recipes_created_at (created_at)
) ENGINE=InnoDB;

-- 4. 꿀조합 구성 상품
CREATE TABLE recipe_items (
    recipe_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2),
    calories DECIMAL(10,2),
    carbohydrate DECIMAL(10,2),
    protein DECIMAL(10,2),
    fat DECIMAL(10,2),
    FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id)
        ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    CHECK (quantity > 0),
    UNIQUE KEY uk_recipe_product (recipe_id, product_id)
) ENGINE=InnoDB;

-- 5. 좋아요 및 즐겨찾기
CREATE TABLE interactions (
    interaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id)
        ON DELETE CASCADE,
    CHECK (type IN ('LIKE', 'BOOKMARK')),
    UNIQUE KEY uk_interactions_user_recipe_type
        (user_id, recipe_id, type),
    INDEX idx_interactions_recipe_type_date
        (recipe_id, type, created_at)
) ENGINE=InnoDB;

-- 6. 댓글 및 리뷰
CREATE TABLE comments (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    rating TINYINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id)
        ON DELETE CASCADE,
    CHECK (type IN ('COMMENT', 'REVIEW')),
    CHECK (
        (type = 'COMMENT' AND rating IS NULL)
        OR
        (type = 'REVIEW' AND rating BETWEEN 1 AND 5)
    ),
    INDEX idx_comments_recipe_type (recipe_id, type)
) ENGINE=InnoDB;

-- 7. 신고
CREATE TABLE reports (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_reports_status (status),
    INDEX idx_reports_target (target_type, target_id)
) ENGINE=InnoDB;

-- 8. 조회 및 즐겨찾기 이력
CREATE TABLE recipe_views (
    activity_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    user_id BIGINT,
    type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id)
        ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE SET NULL,
    CHECK (type IN ('VIEW', 'BOOKMARK')),
    INDEX idx_recipe_views_recipe_type_date
        (recipe_id, type, created_at)
) ENGINE=InnoDB;
