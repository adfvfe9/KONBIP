import client from "./client";
import { db } from "../mocks/recipes";

// true 면 서버 없이 mocks/recipes.js 의 가짜 데이터로 화면만 확인한다. (새로고침하면 초기화)
export const USE_MOCK = false;

const wait = (ms = 250) => new Promise((r) => setTimeout(r, ms));
const clone = (v) => JSON.parse(JSON.stringify(v));
// 담은 상품 → 레시피 시트의 '조합 재료' 표시용
const toIngredients = (items = []) => items.map((i) => ({ name: i.name, quantity: i.quantity }));

/* ───── 서버 응답 → 화면에서 쓰는 모양 ───── */

// 목록 카드: 서버는 recipeId, 화면은 id 를 쓴다.
export const toCard = (r) => ({ ...r, id: r.recipeId });

// 상세: steps 는 문자열 배열, ingredients 는 담은 상품 [{ name, quantity }] 으로 바꾼다.
const toDetail = (r) => ({
  ...r,
  id: r.recipeId,
  steps: (r.steps ?? []).map((s) => s.content),
  ingredients: toIngredients(r.items),
});

export function timeAgo(iso) {
  const sec = Math.max(0, (Date.now() - new Date(iso).getTime()) / 1000);
  if (sec < 60) return "방금 전";
  if (sec < 3600) return `${Math.floor(sec / 60)}분 전`;
  if (sec < 86400) return `${Math.floor(sec / 3600)}시간 전`;
  if (sec < 86400 * 7) return `${Math.floor(sec / 86400)}일 전`;
  if (sec < 86400 * 30) return `${Math.floor(sec / (86400 * 7))}주 전`;
  return new Date(iso).toLocaleDateString("ko-KR");
}

// 댓글: 후기 목록 UI 에 맞춘다. 서버 댓글에는 별점과 좋아요가 없다.
const toComment = (c) => ({
  id: c.commentId,
  authorId: c.authorId,
  nickname: c.author,
  timeText: timeAgo(c.createdAt),
  createdAt: new Date(c.createdAt).getTime(),
  content: c.content,
  rating: null,
  likeCount: 0,
  liked: false,
});

/* ───── 홈·검색 ───── */
export async function getHome() {
  if (USE_MOCK) {
    const popular = [...db.recipes].sort((a, b) => b.bookmarkCount - a.bookmarkCount).slice(0, 3);
    return { popular: clone(popular), latest: clone([...db.recipes].reverse()) };
  }
  const res = await client.get("/api/home");
  return { popular: res.data.popularRecipes.map(toCard), latest: res.data.latestRecipes.map(toCard) };
}

// sort: "latest" | "popular"
export async function searchRecipes(keyword, sort = "latest") {
  if (USE_MOCK) {
    const list = db.recipes.filter(
      (r) => !keyword || r.title.includes(keyword) || r.brand.includes(keyword) || r.tags.some((t) => t.includes(keyword))
    );
    const sorted = sort === "popular" ? [...list].sort((a, b) => b.bookmarkCount - a.bookmarkCount) : [...list].reverse();
    return clone(sorted);
  }
  const res = await client.get("/api/recipes", { params: { keyword, sort, size: 20 } });
  return res.data.content.map(toCard);
}

/* ───── 상세 ───── */
export async function getRecipe(id) {
  if (USE_MOCK) {
    await wait();
    const r = db.recipes.find((x) => x.recipeId === Number(id));
    if (!r) throw { message: "꿀조합을 찾을 수 없어요." };
    return clone(r);
  }
  const res = await client.get(`/api/recipes/${id}`);
  return toDetail(res.data);
}

// 화면의 '댓글 추가하기' 는 서버의 댓글(comments) API 를 쓴다. (리뷰는 회원당 1개 제한이 있어서 맞지 않음)
export async function getComments(id) {
  if (USE_MOCK) { await wait(); return Number(id) === 1 ? clone(db.reviews) : []; }
  const res = await client.get(`/api/recipes/${id}/comments`, { params: { size: 100 } });
  return res.data.content.map(toComment);
}

export async function addComment(id, content) {
  if (USE_MOCK) {
    const c = { id: db.nextId++, nickname: db.me.nickname, timeText: "방금 전", rating: null, content, likeCount: 0, liked: false, createdAt: Date.now() };
    if (Number(id) === 1) db.reviews.unshift(c);
    return clone(c);
  }
  const res = await client.post(`/api/recipes/${id}/comments`, { content });
  return toComment(res.data);
}

export async function toggleBookmark(id, on) {
  if (USE_MOCK) {
    const r = db.recipes.find((x) => x.recipeId === Number(id));
    r.isBookmarked = on;
    r.bookmarkCount += on ? 1 : -1;
    return { isBookmarked: r.isBookmarked, bookmarkCount: r.bookmarkCount };
  }
  const res = on ? await client.post(`/api/recipes/${id}/bookmark`) : await client.delete(`/api/recipes/${id}/bookmark`);
  return res.data; // { isBookmarked, bookmarkCount }
}

/* ───── 내 조합 ───── */
export async function getMyRecipes() {
  if (USE_MOCK) { await wait(); return clone(db.recipes.filter((r) => r.isMine)); }
  const res = await client.get("/api/users/me/recipes", { params: { size: 100 } });
  return res.data.content.map(toCard);
}

// data: { title, items[{ productId, quantity }], brand, tags, steps }
// keep: 수정할 때 화면에 입력칸이 없는 값(description)을 그대로 유지하려고 넘긴다.
function toServerBody(data, keep = {}) {
  return {
    title: data.title,
    description: keep.description ?? null,
    brand: data.brand,
    tags: data.tags,
    steps: data.steps,
    items: data.items.map((i) => ({ productId: i.productId, quantity: i.quantity })),
  };
}

function toForm(data, imageFile, keep) {
  const form = new FormData();
  form.append("recipe", new Blob([JSON.stringify(toServerBody(data, keep))], { type: "application/json" }));
  if (imageFile) form.append("image", imageFile);
  return form;
}

export async function createRecipe(data, imageFile) {
  if (USE_MOCK) {
    await wait(400);
    const r = {
      id: db.nextId++, title: data.title, brand: data.brand, tags: data.tags,
      bookmarkCount: 0, commentCount: 0, imageUrl: imageFile ? URL.createObjectURL(imageFile) : null, description: "",
      items: data.items, ingredients: toIngredients(data.items), steps: data.steps, isMine: true, isBookmarked: false,
    };
    db.recipes.push(r);
    return clone(r);
  }
  const res = await client.post("/api/recipes", toForm(data, imageFile));
  return { id: res.data.recipeId };
}

export async function updateRecipe(id, data, imageFile, keep) {
  if (USE_MOCK) {
    await wait(400);
    const r = db.recipes.find((x) => x.id === Number(id));
    Object.assign(r, { title: data.title, brand: data.brand, tags: data.tags, steps: data.steps, items: data.items, ingredients: toIngredients(data.items) });
    if (imageFile) r.imageUrl = URL.createObjectURL(imageFile);
    return clone(r);
  }
  await client.patch(`/api/recipes/${id}`, toForm(data, imageFile, keep)); // 전체 내용을 보낸다
}

export async function deleteRecipe(id) {
  if (USE_MOCK) {
    await wait(300);
    db.recipes = db.recipes.filter((x) => x.recipeId !== Number(id));
    return;
  }
  await client.delete(`/api/recipes/${id}`);
}
