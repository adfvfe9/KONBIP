-- DB 명세서 v1 이후 추가 항목
-- 요구사항 정의서와 Figma "UI 최종" 화면에는 있지만 V1 스키마에는 없던 데이터를 담는다.
-- 기존 컬럼은 바꾸지 않고 컬럼/테이블 추가만 한다.

-- 상품 가격 (FR-PRD-02, FR-PRD-05). API에 가격이 없으면 NULL로 둔다.
ALTER TABLE products ADD COLUMN price DECIMAL(10,2) NULL;

-- 단종 상품 비활성 처리 (FR-PRD-06). 삭제하지 않고 기존 레시피 참조를 유지한다.
ALTER TABLE products ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

-- 프로필 사진 (FR-MYP-01, IA "프로필 사진 변경")
ALTER TABLE users ADD COLUMN profile_image_data LONGBLOB NULL;
ALTER TABLE users ADD COLUMN profile_image_type VARCHAR(100) NULL;

-- 꿀조합 대표 편의점 (UI "대표 편의점 등록", IA "조합 등록 > 편의점 선택")
ALTER TABLE recipes ADD COLUMN brand VARCHAR(100) NULL;
CREATE INDEX idx_recipes_brand ON recipes (brand);

-- 꿀조합 레시피 단계 (UI "레시피 작성 / STEP 1, STEP 2 / 단계 추가하기")
CREATE TABLE recipe_steps (
    step_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    step_order INT NOT NULL,
    content VARCHAR(500) NOT NULL,
    FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id)
        ON DELETE CASCADE,
    INDEX idx_recipe_steps_recipe_order (recipe_id, step_order)
) ENGINE=InnoDB;

-- 꿀조합 해시태그 (UI "해시태그 추가하기", 카드의 "# 매콤 # 반숙란")
CREATE TABLE recipe_tags (
    tag_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    name VARCHAR(30) NOT NULL,
    FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id)
        ON DELETE CASCADE,
    UNIQUE KEY uk_recipe_tags_recipe_name (recipe_id, name),
    INDEX idx_recipe_tags_name (name)
) ENGINE=InnoDB;
