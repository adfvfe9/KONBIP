// 테스트 데이터 (서버 없이 화면을 돌릴 때 쓴다. api/recipes.js 의 USE_MOCK 참고)
// 사용자가 만든 조합·후기·즐겨찾기·프로필은 sessionStorage 에 저장한다.
// → 새로고침해도 유지되고, 탭(브라우저)을 닫으면 처음 상태로 돌아간다.

// public 폴더에 넣은 이미지. 확장자가 다르면 EXT 한 줄만 바꾸세요. (예: "jpg")
const EXT = "jpeg";
const img = (name) => `/${name}.${EXT}`;

const KEY = "konbip-mock-db-v3";

// 조합 등록의 상품 검색용 (서버 GET /api/products 와 같은 모양)
const PRODUCTS = [
  { productId: 1, name: "몽고탄멘 컵라면", brand: "세븐일레븐", category: "컵라면" },
  { productId: 2, name: "반숙 계란", brand: "세븐일레븐", category: "반찬" },
  { productId: 3, name: "돼지고기 장조림", brand: "세븐일레븐", category: "반찬" },
  { productId: 4, name: "치킨 마요 덮밥", brand: "세븐일레븐", category: "도시락" },
  { productId: 5, name: "연어 주먹밥", brand: "로손", category: "주먹밥" },
  { productId: 6, name: "된장국", brand: "로손", category: "국" },
  { productId: 7, name: "카라아게군", brand: "로손", category: "치킨" },
  { productId: 8, name: "레몬사와", brand: "로손", category: "음료" },
  { productId: 9, name: "푸딩", brand: "패밀리마트", category: "디저트" },
  { productId: 10, name: "식빵", brand: "패밀리마트", category: "빵" },
  { productId: 11, name: "계란 샌드위치", brand: "패밀리마트", category: "샌드위치" },
  { productId: 12, name: "우유", brand: "패밀리마트", category: "음료" },
  { productId: 13, name: "패미치키", brand: "패밀리마트", category: "치킨" },
  { productId: 14, name: "참치마요 주먹밥", brand: "로손", category: "주먹밥" },
];

const item = (productId, quantity = 1) => {
  const p = PRODUCTS.find((x) => x.productId === productId);
  return { productId, name: p.name, brand: p.brand, quantity };
};

const make = (id, title, brand, tags, bookmarkCount, commentCount, extra = {}) => ({
  recipeId: id, title, brand, tags, bookmarkCount, commentCount,
  imageUrl: null, description: "", items: [], steps: [],
  isMine: false, isBookmarked: false, createdAt: id, ...extra,
});

const review = (id, nickname, timeText, rating, content, likeCount, liked, createdAt) =>
  ({ id, nickname, timeText, rating, content, likeCount, liked, createdAt });

function seed() {
  return {
    recipes: [
      make(1, "몽고탄멘 조합", "세븐일레븐", ["매콤", "반숙란", "장조림"], 625, 6, {
        isMine: true, imageUrl: img("ahdrh"),
        description: "몽고탄멘 조합은 일본 세븐일레븐의 매콤한\n몽고탄멘에 돼지고기 장조림과 반숙란을 더한 인기 편의점 꿀조합",
        items: [item(1), item(2), item(3)],
        steps: ["준비한 몽고탄멘을 조리법 대로 조리해주세요", "조리 된 몽고탄멘 안에 반숙 계란, 돼지고기 장조림을 섞어 주세요"],
      }),
      make(2, "연어 주먹밥 세트", "로손", ["든든", "간단"], 410, 2, {
        isMine: true, imageUrl: img("dusdj"), items: [item(5), item(6)],
        steps: ["주먹밥을 꺼내 포장을 벗겨요", "된장국에 물을 부어 함께 먹어요"],
      }),
      make(3, "푸딩 샌드 조합", "패밀리마트", ["디저트", "달콤"], 388, 0, {
        isMine: true, imageUrl: img("vneld"), items: [item(9), item(10)], steps: ["식빵 사이에 푸딩을 올려요"],
      }),
      make(4, "치킨 마요 덮밥 조합", "세븐일레븐", ["든든", "마요"], 250, 0, {
        isBookmarked: true, items: [item(4), item(2)], steps: ["덮밥을 데워요", "반숙 계란을 올려 비벼 먹어요"],
      }),
      make(5, "카라아게 + 레몬사와", "로손", ["안주", "상큼"], 190, 0, {
        isBookmarked: true, items: [item(7), item(8)], steps: ["카라아게군을 데워요", "시원한 레몬사와와 함께 즐겨요"],
      }),
      make(6, "계란샌드 + 우유", "패밀리마트", ["아침", "부드러움"], 120, 0, {
        items: [item(11), item(12)], steps: ["샌드위치와 우유를 함께 먹어요"],
      }),
    ],
    // 조합별 후기 { [recipeId]: [...] }
    reviews: {
      1: [
        review(1, "라멘", "15시간 전", 4, "체다 치즈 추가하면 더 맛있어져요ㅠㅠ", 12, true, 6),
        review(2, "비둘기", "1일 전", 5, "반숙란 터뜨려서 국물에 풀어 먹으니까 진짜 꿀맛이에요!", 9, false, 5),
        review(3, "콘삡러", "2일 전", 3, "저한테는 조금 매웠어요. 우유랑 같이 먹으면 딱 좋아요", 4, false, 4),
        review(4, "편의점탐험가", "3일 전", 5, "장조림 올리니까 든든해서 한 끼로 충분했어요", 7, true, 3),
        review(5, "여행중", "5일 전", 4, "일본 여행 가서 바로 따라 해봤는데 대성공이었어요 ㅎㅎ", 2, false, 2),
        review(6, "야식요정", "1주 전", 4, "새벽에 먹었는데 후회 없는 조합이에요. 김가루도 추천!", 1, false, 1),
      ],
      2: [
        review(7, "라멘", "2일 전", 5, "된장국이랑 먹으니까 아침으로 딱이에요", 3, false, 2),
        review(8, "비둘기", "4일 전", 4, "간단해서 자주 해 먹어요!", 1, false, 1),
      ],
    },
    products: PRODUCTS,
    me: { nickname: "콘삡", profileImageUrl: null },
    nextId: 100,
  };
}

function load() {
  try {
    const saved = sessionStorage.getItem(KEY);
    if (saved) return JSON.parse(saved);
  } catch { /* 저장소를 못 쓰면 기본 데이터로 */ }
  return seed();
}

export const db = load();

// 데이터를 바꾼 뒤 호출한다.
export function saveDb() {
  try {
    sessionStorage.setItem(KEY, JSON.stringify(db));
  } catch {
    /* 용량 초과 등: 이번 화면에서만 유지된다 */
  }
}

// 회원 탈퇴 등: 처음 상태로 되돌린다.
export function resetDb() {
  try { sessionStorage.removeItem(KEY); } catch { /* 무시 */ }
  Object.assign(db, seed());
}

// 사진 파일 → 작게 줄인 data URL (sessionStorage 에 넣을 수 있게 긴 변 800px, JPEG)
export function fileToDataUrl(file, maxSize = 800) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file);
    const image = new Image();
    image.onload = () => {
      const scale = Math.min(1, maxSize / Math.max(image.width, image.height));
      const canvas = document.createElement("canvas");
      canvas.width = Math.round(image.width * scale);
      canvas.height = Math.round(image.height * scale);
      canvas.getContext("2d").drawImage(image, 0, 0, canvas.width, canvas.height);
      URL.revokeObjectURL(url);
      resolve(canvas.toDataURL("image/jpeg", 0.8));
    };
    image.onerror = () => { URL.revokeObjectURL(url); reject({ message: "사진을 읽지 못했어요." }); };
    image.src = url;
  });
}
