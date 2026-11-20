# Konbip Backend

일본 편의점 꿀조합 추천·공유 서비스 **Konbip**의 REST API 서버입니다.

| 구분 | 내용 |
| --- | --- |
| 언어 / 프레임워크 | Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation) |
| DB | MySQL 8 (스키마는 Flyway 가 관리) |
| 인증 | JWT (access 30분 / refresh 14일), Google·Kakao 소셜 로그인 |
| 빌드 | Gradle (wrapper 포함) |

---

## 1. 실행하기

### 준비물
- JDK 21
- MySQL 8 (로컬에서 실행 중이어야 함)

### 실행
```bash
cd backend
# Windows
gradlew.bat bootRun
# macOS / Linux
./gradlew bootRun
```

- 기본 접속 정보: `jdbc:mysql://localhost:3306/conbip`, 사용자 `root`, 비밀번호 없음
- DB `conbip` 이 없으면 자동으로 만들고, Flyway 가 테이블을 생성합니다.
- 내 PC의 MySQL 비밀번호가 다르면 환경변수로 넘기거나, `backend/application-secret.yml` 파일을 만들어 넣으세요. 이 파일은 git 에 올라가지 않습니다.

```yaml
# backend/application-secret.yml
spring:
  datasource:
    password: 내_MySQL_비밀번호
```

서버 주소는 `http://localhost:8080` 입니다.

### 로컬 샘플 데이터
기본 프로필(`local`)로 실행하면 **DB가 비어 있을 때 한 번만** 샘플 데이터가 들어갑니다. 상품 15개, 회원 4명, 꿀조합 5개, 좋아요·리뷰·댓글이 포함돼요.
샘플 상품의 가격과 영양성분은 화면 개발용 근사치이고, 실제 정보가 아닙니다.

| 이메일 | 닉네임 | 권한 | 비밀번호 |
| --- | --- | --- | --- |
| demo@konbip.dev | 콘삡 | USER | `konbip1234!` |
| ramen@konbip.dev | 라멘 | USER | `konbip1234!` |
| dove@konbip.dev | 비둘기 | USER | `konbip1234!` |
| admin@konbip.dev | 관리자 | ADMIN | `konbip1234!` |

### 테스트
```bash
./gradlew test
```
MySQL 없이 H2(MySQL 호환 모드)로 돌아갑니다. 실제 Flyway 마이그레이션(V1, V2)을 그대로 적용한 뒤 API를 호출해서 검증해요.

---

## 2. 환경변수

| 변수 | 기본값 | 설명 |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/conbip?...` | MySQL 접속 URL |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / (빈 값) | DB 계정 |
| `JWT_SECRET` | 개발용 값 | **배포 시 반드시 변경.** 32바이트 이상 임의 문자열 |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | 프론트 주소. 쉼표 구분, `https://*.vercel.app` 같은 패턴 가능 |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | (없음) | Google 로그인 |
| `KAKAO_CLIENT_ID` / `KAKAO_CLIENT_SECRET` | (없음) | Kakao 로그인 (REST API 키) |
| `OAUTH_REDIRECT_BASE_URI` | `http://localhost:8080` | 백엔드 주소. 콜백은 `{이 값}/api/auth/oauth2/callback/{google\|kakao}` |
| `OAUTH_FRONTEND_REDIRECT_URI` | `http://localhost:5173/oauth/callback` | 소셜 로그인 후 돌아갈 프론트 주소 |
| `TABECAL_BASE_URL` / `TABECAL_API_KEY` | (없음) | 상품 데이터 API |
| `PRODUCT_SYNC_ENABLED` | `false` | `true` 면 매일 04:00(KST)에 상품 동기화 |
| `SPRING_PROFILES_ACTIVE` | `local` | 배포 시 `prod` (DB·JWT·CORS 값을 기본값 없이 환경변수로만 받음) |

---

## 3. 프론트엔드 연동 가이드

### 공통 응답
```json
{ "success": true, "message": "요청이 성공적으로 처리되었습니다.", "data": { } }
{ "success": false, "code": "RECIPE_NOT_FOUND", "message": "꿀조합을 찾을 수 없습니다.", "data": null }
```
- 실패 응답에는 `code` 가 붙습니다. 화면 분기는 `message` 대신 `code` 로 하세요.
- 목록은 `data` 안에 `content`, `page`(0부터), `size`, `totalElements`, `totalPages` 가 들어 있어요.

### 인증
- 로그인/회원가입 응답의 `accessToken` 을 `Authorization: Bearer {accessToken}` 헤더로 보냅니다.
- `401` + `code: INVALID_TOKEN` 이 오면 `POST /api/auth/refresh` 에 `{ "refreshToken": "..." }` 를 보내 새 토큰을 받고 다시 요청하세요.
- 로그아웃은 서버에 토큰을 저장하지 않는 방식이라, 프론트에서 토큰을 지우면 됩니다.

### 꿀조합 등록 (사진 포함)
```js
const form = new FormData();
form.append("recipe", new Blob([JSON.stringify({
  title: "몽고탄멘 조합",
  description: "매콤한 몽고탄멘에 ...",
  brand: "세븐일레븐",            // 대표 편의점
  tags: ["매콤", "반숙란"],         // '#' 은 있어도 없어도 됨
  steps: ["몽고탄멘을 조리한다", "반숙란과 장조림을 올린다"],
  items: [
    { productId: 1, quantity: 1 },
    { productId: 3, quantity: 2, customPrice: 300 }  // 상품에 가격 정보가 없을 때만 쓰임
  ]
})], { type: "application/json" }));
if (file) form.append("image", file);  // jpg/png, 5MB 이하 (선택)

await fetch(`${API}/api/recipes`, { method: "POST", headers: { Authorization: `Bearer ${token}` }, body: form });
```
- `recipe` 는 Blob 대신 `form.append("recipe", JSON.stringify(...))` 처럼 문자열로 보내도 됩니다.
- 수정은 `PATCH /api/recipes/{id}` 에 같은 형식으로 **전체 내용**을 보냅니다. 사진을 지우려면 `form.append("removeImage", "true")` 를 추가하세요.
- 사진이 없으면 `POST /api/recipes` 에 JSON 본문으로 보내도 됩니다.

### 이미지
`imageUrl`, `profileImageUrl` 은 `/api/recipes/1/image` 같은 상대 경로입니다. 앞에 API 서버 주소를 붙여서 쓰세요. 값이 `null` 이면 기본 이미지를 보여 주면 됩니다.

### 영양성분 · 가격 표시
- 상세의 `nutrition` 은 상품 1개 기준 값 × 수량을 합친 값입니다.
- `nutrition.complete == false` 면 일부 상품의 영양정보가 없는 상태예요. 이때는 "일부 상품의 영양정보가 없어 실제보다 적을 수 있어요" 같은 안내를 보여 주세요. 어떤 항목이 빠졌는지는 `missingNutrients` 에 들어 있습니다.
- `priceComplete == false` 면 가격이 없는 상품이 있어서 `totalPrice` 에서 빠졌다는 뜻입니다.
- 조합 만들기 화면(SC-07)의 실시간 합계는 상품 목록 응답의 영양성분으로 프론트에서 바로 계산하면 됩니다. 계산 규칙은 같아요. 값이 `null` 인 상품은 빼고 더합니다.

### 소셜 로그인
1. 로그인 버튼을 누르면 `window.location.href = API + "/api/auth/oauth2/google"` (또는 `kakao`) 로 이동합니다.
2. 로그인이 끝나면 `OAUTH_FRONTEND_REDIRECT_URI` 로 돌아와요. 이때 토큰은 URL `#` 뒤에 붙어 옵니다.
   - 성공: `/oauth/callback#accessToken=...&refreshToken=...&newUser=true`
   - 실패: `/oauth/callback#error=OAUTH_FAILED`
3. `newUser=true` 면 닉네임 설정 화면을 띄우고 `PATCH /api/users/me` 로 닉네임을 바꿔 주세요. 처음에는 `콘빕123456` 같은 임시 닉네임이 들어가 있어요.

---

## 4. API 목록

모든 경로 앞에 `/api` 가 붙습니다. 권한: 전체 = 비회원 가능, 회원 = 로그인 필요, 작성자 = 본인만, 관리자 = ADMIN.

### 회원·인증
| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| POST | `/auth/signup` | 회원가입 (가입 즉시 토큰 발급) | 전체 |
| POST | `/auth/login` | 로그인 | 전체 |
| POST | `/auth/logout` | 로그아웃 | 회원 |
| POST | `/auth/refresh` | 토큰 갱신 | 전체 |
| GET | `/auth/oauth2/{google\|kakao}` | 소셜 로그인 시작 (302) | 전체 |
| GET | `/auth/oauth2/callback/{provider}` | 소셜 로그인 콜백 (302) | 전체 |
| GET | `/users/me` | 내 정보 | 회원 |
| PATCH | `/users/me` | 닉네임 수정 `{nickname}` | 회원 |
| PATCH | `/users/me/password` ➕ | 비밀번호 변경 `{currentPassword, newPassword}` | 회원(이메일 가입) |
| DELETE | `/users/me` ➕ | 회원 탈퇴 (내 꿀조합·리뷰·댓글·좋아요 삭제) | 회원 |
| PUT | `/users/me/profile-image` ➕ | 프로필 사진 변경 (multipart `image`) | 회원 |
| DELETE | `/users/me/profile-image` ➕ | 프로필 사진 삭제 | 회원 |
| GET | `/users/{userId}/profile-image` ➕ | 프로필 사진 | 전체 |

### 홈·상품
| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/home` | 인기(좋아요순) 10개 + 최신 10개 | 전체 |
| GET | `/products?keyword=&brand=&category=&page=&size=` | 상품 목록·검색 (판매 중인 상품만) | 전체 |
| GET | `/products/{productId}` | 상품 상세 | 전체 |
| GET | `/meta/brands` | 편의점 목록 | 전체 |
| GET | `/meta/categories?brand=` | 카테고리 목록 | 전체 |

### 꿀조합
| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/recipes?keyword=&brand=&category=&tag=&minRating=&sort=&page=&size=` | 목록·검색 | 전체 |
| GET | `/recipes/{recipeId}` | 상세 (조회수 집계, 로그인 시 `isLiked` 등 포함) | 전체 |
| POST | `/recipes` | 등록 (multipart 또는 JSON) | 회원 |
| PATCH | `/recipes/{recipeId}` | 수정 (multipart 또는 JSON) | 작성자 |
| DELETE | `/recipes/{recipeId}` | 삭제 | 작성자 |
| GET | `/recipes/{recipeId}/image` | 대표 이미지 | 전체 |

- `sort`: `latest`(기본), `popular`(좋아요), `rating`(별점), `views` ➕(조회수), `bookmarks` ➕(즐겨찾기)
- `keyword`: 제목, 설명, 해시태그, 구성 상품명에서 찾아요.
- `brand`: 대표 편의점이 같거나, 그 편의점 상품이 하나라도 들어간 꿀조합을 찾아요.
- `tag` ➕: 해시태그가 정확히 같은 꿀조합을 찾아요.

### 좋아요·즐겨찾기·댓글·리뷰·신고
| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| POST / DELETE | `/recipes/{id}/like` | 좋아요 등록/취소 → `{isLiked, likeCount}` | 회원 |
| POST / DELETE | `/recipes/{id}/bookmark` | 즐겨찾기 등록/취소 → `{isBookmarked, bookmarkCount}` | 회원 |
| GET | `/recipes/{id}/comments` | 댓글 목록 (최신순) | 전체 |
| POST | `/recipes/{id}/comments` | 댓글 작성 `{content}` (1~300자) | 회원 |
| PATCH / DELETE | `/comments/{commentId}` | 댓글 수정/삭제 | 작성자 |
| GET | `/recipes/{id}/reviews` | 리뷰 목록 (최신순) | 전체 |
| POST | `/recipes/{id}/reviews` | 리뷰 작성 `{rating 1~5, content}` | 회원 |
| PATCH / DELETE | `/reviews/{reviewId}` | 리뷰 수정/삭제 | 작성자 |
| POST | `/reports` | 신고 `{targetType: RECIPE\|COMMENT, targetId, reason}` | 회원 |

- 좋아요와 즐겨찾기는 같은 요청을 여러 번 보내도 오류 없이 현재 상태를 돌려줍니다.
- 리뷰는 회원당 꿀조합 하나에 1개만 쓸 수 있어요. 내 꿀조합에는 쓸 수 없습니다.
- 같은 대상은 한 번만 신고할 수 있어요.

### 랭킹·마이페이지
| Method | Endpoint | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/rankings?period=&type=&page=&size=` | 랭킹 → `[{rank, score, recipe}]` | 전체 |
| GET | `/users/me/recipes` | 내가 쓴 꿀조합 | 회원 |
| GET | `/users/me/likes` | 좋아요한 꿀조합 (최근 누른 순) | 회원 |
| GET | `/users/me/bookmarks` | 즐겨찾기한 꿀조합 ("저장한 조합") | 회원 |
| GET | `/users/me/reviews` | 내가 쓴 리뷰 (`recipeTitle` 포함) | 회원 |

- `period`: `daily`(최근 1일), `weekly`(최근 7일, 기본), `monthly`(최근 30일), `all` ➕(전체 기간)
- `type`: `views`(기본), `bookmarks`, `rating`. 별점 랭킹은 기간 안에 리뷰가 3개 이상인 꿀조합만 포함해요.

### 관리자
| Method | Endpoint | 설명 |
| --- | --- | --- |
| GET | `/admin/reports?status=PENDING\|RESOLVED\|REJECTED` | 신고 목록 |
| PATCH | `/admin/reports/{reportId}` | 신고 처리 `{status}` |
| DELETE | `/admin/recipes/{recipeId}` | 꿀조합 삭제 |
| DELETE | `/admin/comments/{commentId}` | 댓글·리뷰 삭제 |
| POST | `/admin/products/sync` | 상품 데이터 수동 동기화 |

관리자 계정은 DB에서 직접 지정합니다: `UPDATE users SET role = 'ADMIN' WHERE email = '...';`

➕ 표시는 Notion API 명세서에 없던 항목입니다. 아래 5절을 보세요.

---

## 5. Notion 명세서 대비 변경·추가 사항

### DB (`src/main/resources/db/migration`)
- `V1__init_schema.sql`: Notion **8. DB 명세서** SQL 그대로입니다.
- `V2__design_additions.sql`: 요구사항 정의서와 Figma "UI 최종" 화면에는 있는데 V1 스키마에 없던 항목을 추가했어요. 기존 컬럼은 바꾸지 않았습니다.

| 추가 | 근거 |
| --- | --- |
| `products.price` | 상품 가격 표시, API 가격 우선 (FR-PRD-02, 05) |
| `products.active` | 단종 상품은 삭제하지 않고 비활성 처리 (FR-PRD-06) |
| `users.profile_image_data`, `profile_image_type` | 프로필 사진 변경 (FR-MYP-01, IA) |
| `recipes.brand` | UI "대표 편의점 등록", 카드의 편의점 표시 |
| `recipe_steps` 테이블 | UI "레시피 작성 / STEP 1, 2 / 단계 추가하기" |
| `recipe_tags` 테이블 | UI "해시태그 추가하기", 카드의 `# 매콤` |

Notion SQL로 이미 테이블을 만든 DB라면, 서버를 켤 때 Flyway가 V1을 건너뛰고 V2만 적용합니다(`baseline-on-migrate`).

### API
- 위 표의 ➕ 항목(비밀번호 변경, 탈퇴, 프로필 사진, 정렬·필터 추가, `period=all`)을 추가했어요. 요구사항 정의서에 있지만 API 명세서에 빠져 있던 것들입니다.
- 실패 응답에 `code` 필드를 추가했어요.
- 꿀조합 상세에 `brand`, `tags`, `steps`, `nutrition`, `priceComplete`, `commentCount`, `viewCount`, `isReviewed`, `isMine`, `authorId` 를 추가했어요. 명세서 예시의 필드는 그대로 있습니다.
- `recipe_items` 의 영양성분은 **등록 당시 상품 1개 기준** 값입니다. 합계는 값에 수량을 곱해서 구해요.

### 권한: 문서끼리 다른 부분
- **API 명세서**는 상품·꿀조합 조회를 "전체(비회원 가능)"로 정했어요.
- **기획서와 요구사항 정의서 2.2**는 비회원이 조회도 못 하게 정했어요.
- 지금은 **API 명세서를 따라** 조회 API를 공개해 두었습니다. 비회원 조회를 막기로 정하면 `SecurityConfig` 의 `PUBLIC_GET` 목록만 줄이면 돼요.

---

## 6. 남은 일

- [ ] **Tabecal 연동**: API Key를 받으면 `product/sync/TabecalProductSource.fetchAll()` 에 응답 매핑을 구현해야 해요. 응답 형식을 아직 몰라서 추측으로 만들지 않았습니다. 저장, 갱신, 단종 처리, 하루 1회 스케줄, 관리자 수동 동기화는 이미 연결되어 있어요.
- [ ] **소셜 로그인 키 등록**: Google Cloud Console과 Kakao Developers에 앱을 등록하고, Redirect URI를 `{백엔드 주소}/api/auth/oauth2/callback/google` (`/kakao`) 로 넣은 뒤 환경변수에 키를 설정해야 해요. 실제 제공자 서버와의 연동은 키가 없어서 아직 테스트하지 못했습니다.
- [ ] **배포**: `SPRING_PROFILES_ACTIVE=prod` 로 실행하고, `DB_*`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`(Vercel 주소), `OAUTH_*` 환경변수를 설정해야 해요.
- [ ] refresh 토큰은 서버에 저장하지 않아요. 그래서 로그아웃해도 이미 발급된 토큰은 만료될 때까지 유효합니다. 필요하면 토큰 저장 테이블을 추가하세요.

---

## 7. 코드 구조

```
com.konbip
├── auth        회원가입·로그인·토큰 갱신, oauth/ 소셜 로그인
├── user        회원, 내 정보, 마이페이지, 탈퇴, 프로필 사진
├── product     상품 조회, meta(브랜드/카테고리), sync/ 외부 상품 동기화
├── recipe      꿀조합 CRUD, 검색·정렬 쿼리(RecipeQueryRepository), 영양 합산
├── interaction 좋아요·즐겨찾기, 조회 이력(recipe_views)
├── comment     댓글·리뷰
├── report      신고
├── ranking     랭킹
├── home        홈 화면
├── admin       관리자 API
├── dev         로컬 샘플 데이터
└── global      공통 응답·예외, 보안(JWT), 이미지 저장, 설정
```
