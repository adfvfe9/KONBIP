import client from "./client";
import { db } from "../mocks/recipes";
import { USE_MOCK } from "./recipes";

const wait = (ms = 200) => new Promise((r) => setTimeout(r, ms));
const clone = (v) => JSON.parse(JSON.stringify(v));

// { nickname, profileImageUrl, ... }  TODO: 서버 응답 필드명 확인
export async function getMe() {
  if (USE_MOCK) { await wait(); return clone(db.me); }
  const res = await client.get("/api/users/me");
  return res.data;
}

export async function updateNickname(nickname) {
  if (USE_MOCK) { await wait(); db.me.nickname = nickname; return clone(db.me); }
  const res = await client.patch("/api/users/me", { nickname });
  return res.data;
}

export async function updateProfileImage(file) {
  if (USE_MOCK) { await wait(); db.me.profileImageUrl = URL.createObjectURL(file); return clone(db.me); }
  const form = new FormData();
  form.append("image", file);
  const res = await client.put("/api/users/me/profile-image", form);
  return res.data;
}

export async function getSavedRecipes() {
  if (USE_MOCK) { await wait(); return clone(db.recipes.filter((r) => r.isBookmarked)); }
  const res = await client.get("/api/users/me/bookmarks");
  return res.data.content ?? res.data;
}

export async function deleteAccount() {
  if (USE_MOCK) { await wait(300); return; }
  await client.delete("/api/users/me");
}
