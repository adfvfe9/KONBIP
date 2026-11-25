import client from "./client";
import { db } from "../mocks/recipes";

// 서버가 준비되면 false 로 바꾸세요.
export const USE_MOCK = true;

const wait = (ms = 250) => new Promise((r) => setTimeout(r, ms));
const clone = (v) => JSON.parse(JSON.stringify(v));
const splitIngredients = (text = "") =>
  text.split(/[,，\n]/).map((s) => s.trim()).filter(Boolean).map((name) => ({ name }));

/* ───── 홈·검색 ───── */
export async function getHome() {
  if (USE_MOCK) {
    const popular = [...db.recipes].sort((a, b) => b.bookmarkCount - a.bookmarkCount).slice(0, 3);
    return { popular: clone(popular), latest: clone([...db.recipes].reverse()) };
  }
  const res = await client.get("/api/home");
  return res.data; // TODO: 응답 필드명(popular/latest 등)을 확인해서 맞추기
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
  return res.data.content;
}

/* ───── 상세 ───── */
export async function getRecipe(id) {
  if (USE_MOCK) {
    await wait();
    const r = db.recipes.find((x) => x.id === Number(id));
    if (!r) throw { message: "꿀조합을 찾을 수 없어요." };
    return clone(r);
  }
  const res = await client.get(`/api/recipes/${id}`);
  return res.data; // TODO: ingredients/steps/isMine 등 필드명 확인
}

export async function getReviews(id) {
  if (USE_MOCK) { await wait(); return clone(db.reviews); }
  const res = await client.get(`/api/recipes/${id}/reviews`);
  return res.data.content ?? res.data;
}

// 디자인에는 별점 선택 UI가 없어서 우선 5점으로 보낸다. (디자인 확정 후 수정)
export async function addReview(id, content, rating = 5) {
  if (USE_MOCK) {
    const review = { id: db.nextId++, nickname: "콘삡", timeText: "방금 전", rating, content, likeCount: 0, liked: false, createdAt: Date.now() };
    db.reviews.unshift(review);
    return clone(review);
  }
  const res = await client.post(`/api/recipes/${id}/reviews`, { rating, content });
  return res.data;
}

export async function toggleBookmark(id, on) {
  if (USE_MOCK) {
    const r = db.recipes.find((x) => x.id === Number(id));
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
  const res = await client.get("/api/users/me/recipes");
  return res.data.content ?? res.data;
}

// data: { title, ingredientsText, brand, tags, steps }
function toServerBody(data) {
  return {
    title: data.title,
    description: data.ingredientsText, // 디자인의 '재료' 입력을 설명으로 보낸다
    brand: data.brand,
    tags: data.tags,
    steps: data.steps,
    items: [], // TODO: 백엔드는 상품 선택(productId)을 요구한다. 재료 방식이 정해지면 수정
  };
}

function toForm(data, imageFile) {
  const form = new FormData();
  form.append("recipe", new Blob([JSON.stringify(toServerBody(data))], { type: "application/json" }));
  if (imageFile) form.append("image", imageFile);
  return form;
}

export async function createRecipe(data, imageFile) {
  if (USE_MOCK) {
    await wait(400);
    const r = {
      id: db.nextId++, title: data.title, brand: data.brand, tags: data.tags,
      bookmarkCount: 0, commentCount: 0, imageUrl: null, description: "",
      ingredients: splitIngredients(data.ingredientsText), steps: data.steps, isMine: true, isBookmarked: false,
    };
    db.recipes.push(r);
    return clone(r);
  }
  const res = await client.post("/api/recipes", toForm(data, imageFile));
  return res.data;
}

export async function updateRecipe(id, data, imageFile) {
  if (USE_MOCK) {
    await wait(400);
    const r = db.recipes.find((x) => x.id === Number(id));
    Object.assign(r, { title: data.title, brand: data.brand, tags: data.tags, steps: data.steps, ingredients: splitIngredients(data.ingredientsText) });
    return clone(r);
  }
  const res = await client.patch(`/api/recipes/${id}`, toForm(data, imageFile)); // 전체 내용을 보낸다
  return res.data;
}

export async function deleteRecipe(id) {
  if (USE_MOCK) {
    await wait(300);
    db.recipes = db.recipes.filter((x) => x.id !== Number(id));
    return;
  }
  await client.delete(`/api/recipes/${id}`);
}
