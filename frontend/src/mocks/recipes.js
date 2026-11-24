// 화면 개발용 가짜 데이터(메모리). 새로고침하면 처음 상태로 돌아갑니다.
// 필드명은 서버 응답을 확인한 뒤 맞춰 주세요.
const make = (id, title, brand, tags, bookmarkCount, commentCount, extra = {}) => ({
  id, title, brand, tags, bookmarkCount, commentCount,
  imageUrl: null, description: "", ingredients: [], steps: [],
  isMine: false, isBookmarked: false, ...extra,
});

export const db = {
  recipes: [
    make(1, "몽고탄멘 조합", "세븐일레븐", ["매콤", "반숙란", "장조림"], 625, 57, {
      isMine: true,
      description: "몽고탄멘 조합은 일본 세븐일레븐의 매콤한\n몽고탄멘에 돼지고기 장조림과 반숙란을 더한 인기 편의점 꿀조합",
      ingredients: [{ name: "몽고탄멘 라면" }, { name: "반숙 계란" }, { name: "돼지고기 장조림" }],
      steps: ["준비한 몽고탄멘을 조리법 대로 조리해주세요", "조리 된 몽고탄멘 안에 반숙 계란, 돼지고기 장조림을 섞어 주세요"],
    }),
    make(2, "연어 주먹밥 세트", "로손", ["든든", "간단"], 410, 33, {
      isMine: true, ingredients: [{ name: "연어 주먹밥" }, { name: "된장국" }], steps: ["주먹밥을 꺼내 포장을 벗겨요", "된장국에 물을 부어 함께 먹어요"],
    }),
    make(3, "푸딩 샌드 조합", "패밀리마트", ["디저트", "달콤"], 388, 21, {
      isMine: true, ingredients: [{ name: "푸딩" }, { name: "식빵" }], steps: ["식빵 사이에 푸딩을 올려요"],
    }),
    make(4, "치킨 마요 덮밥 조합", "세븐일레븐", ["든든", "마요"], 250, 18, { isBookmarked: true }),
    make(5, "카라아게 + 레몬사와", "로손", ["안주", "상큼"], 190, 12, { isBookmarked: true }),
    make(6, "계란샌드 + 우유", "패밀리마트", ["아침", "부드러움"], 120, 9),
  ],
  reviews: [
    { id: 1, nickname: "라멘", timeText: "15시간 전", rating: 4, content: "체다 치즈 추가하면 더 맛있어져요ㅠㅠ", likeCount: 5, liked: true, createdAt: 4 },
    { id: 2, nickname: "라멘", timeText: "15시간 전", rating: 4, content: "체다 치즈 추가하면 더 맛있어져요ㅠㅠ", likeCount: 5, liked: false, createdAt: 3 },
    { id: 3, nickname: "라멘", timeText: "15시간 전", rating: 4, content: "체다 치즈 추가하면 더 맛있어져요ㅠㅠ", likeCount: 5, liked: true, createdAt: 2 },
    { id: 4, nickname: "라멘", timeText: "15시간 전", rating: 4, content: "체다 치즈 추가하면 더 맛있어져요ㅠㅠ", likeCount: 5, liked: false, createdAt: 1 },
  ],
  me: { nickname: "콘삡", profileImageUrl: null },
  nextId: 100,
};
