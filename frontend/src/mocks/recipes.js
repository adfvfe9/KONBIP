// 화면 개발용 가짜 데이터(메모리). 새로고침하면 처음 상태로 돌아갑니다.
// 필드명은 서버 응답을 확인한 뒤 맞춰 주세요.
// public 폴더에 넣은 이미지. 확장자가 다르면 EXT 한 줄만 바꾸세요. (예: "jpg")
const EXT = "jpeg";
const img = (name) => `/${name}.${EXT}`;

const make = (id, title, brand, tags, bookmarkCount, commentCount, extra = {}) => ({
  recipeId: id, title, brand, tags, bookmarkCount, commentCount,
  imageUrl: null, description: "", ingredients: [], steps: [],
  isMine: false, isBookmarked: false, ...extra,
});

export const db = {
  recipes: [
    make(1, "몽고탄멘 조합", "세븐일레븐", ["매콤", "반숙란", "장조림"], 625, 57, {
      isMine: true, imageUrl: img("ahdrh"),
      description: "몽고탄멘 조합은 일본 세븐일레븐의 매콤한\n몽고탄멘에 돼지고기 장조림과 반숙란을 더한 인기 편의점 꿀조합",
      ingredients: [{ name: "몽고탄멘 라면" }, { name: "반숙 계란" }, { name: "돼지고기 장조림" }],
      steps: ["준비한 몽고탄멘을 조리법 대로 조리해주세요", "조리 된 몽고탄멘 안에 반숙 계란, 돼지고기 장조림을 섞어 주세요"],
    }),
    make(2, "연어 주먹밥 세트", "로손", ["든든", "간단"], 410, 33, {
      isMine: true, imageUrl: img("dusdj"), ingredients: [{ name: "연어 주먹밥" }, { name: "된장국" }], steps: ["주먹밥을 꺼내 포장을 벗겨요", "된장국에 물을 부어 함께 먹어요"],
    }),
    make(3, "푸딩 샌드 조합", "패밀리마트", ["디저트", "달콤"], 388, 21, {
      isMine: true, imageUrl: img("vneld"), ingredients: [{ name: "푸딩" }, { name: "식빵" }], steps: ["식빵 사이에 푸딩을 올려요"],
    }),
    make(4, "치킨 마요 덮밥 조합", "세븐일레븐", ["든든", "마요"], 250, 18, { isBookmarked: true }),
    make(5, "카라아게 + 레몬사와", "로손", ["안주", "상큼"], 190, 12, { isBookmarked: true }),
    make(6, "계란샌드 + 우유", "패밀리마트", ["아침", "부드러움"], 120, 9),
  ],
  reviews: [
    { id: 1, nickname: "라멘", timeText: "15시간 전", rating: 4, content: "체다 치즈 추가하면 더 맛있어져요ㅠㅠ", likeCount: 12, liked: true, createdAt: 6 },
    { id: 2, nickname: "비둘기", timeText: "1일 전", rating: 5, content: "반숙란 터뜨려서 국물에 풀어 먹으니까 진짜 꿀맛이에요!", likeCount: 9, liked: false, createdAt: 5 },
    { id: 3, nickname: "콘삡러", timeText: "2일 전", rating: 3, content: "저한테는 조금 매웠어요. 우유랑 같이 먹으면 딱 좋아요", likeCount: 4, liked: false, createdAt: 4 },
    { id: 4, nickname: "편의점탐험가", timeText: "3일 전", rating: 5, content: "장조림 올리니까 든든해서 한 끼로 충분했어요", likeCount: 7, liked: true, createdAt: 3 },
    { id: 5, nickname: "여행중", timeText: "5일 전", rating: 4, content: "일본 여행 가서 바로 따라 해봤는데 대성공이었어요 ㅎㅎ", likeCount: 2, liked: false, createdAt: 2 },
    { id: 6, nickname: "야식요정", timeText: "1주 전", rating: 4, content: "새벽에 먹었는데 후회 없는 조합이에요. 김가루도 추천!", likeCount: 1, liked: false, createdAt: 1 },
  ],
  me: { nickname: "콘삡", profileImageUrl: null },
  nextId: 100,
};
