import client from "./client";
import { db } from "../mocks/recipes";
import { USE_MOCK, toCard } from "./recipes";

const wait = (ms = 200) => new Promise((r) => setTimeout(r, ms));
const clone = (v) => JSON.parse(JSON.stringify(v));

// 프로필 사진은 바꿔도 주소(/api/users/{id}/profile-image)가 같아서 브라우저가 예전 사진을 보여 줄 수 있다.
// 사진을 바꿀 때마다 주소 뒤에 ?v= 를 바꿔 붙여서 새로 받게 한다.
let photoVersion = Date.now();
const toMe = (u) => ({ ...u, profileImageUrl: u.profileImageUrl ? `${u.profileImageUrl}?v=${photoVersion}` : null });

// { userId, email, nickname, profileImageUrl, provider, ... }
export async function getMe() {
  if (USE_MOCK) { await wait(); return clone(db.me); }
  const res = await client.get("/api/users/me");
  return toMe(res.data);
}

export async function updateNickname(nickname) {
  if (USE_MOCK) { await wait(); db.me.nickname = nickname; return clone(db.me); }
  const res = await client.patch("/api/users/me", { nickname });
  return toMe(res.data);
}

export async function updateProfileImage(file) {
  if (USE_MOCK) { await wait(); db.me.profileImageUrl = URL.createObjectURL(file); return clone(db.me); }
  const form = new FormData();
  form.append("image", file);
  const res = await client.put("/api/users/me/profile-image", form);
  photoVersion = Date.now();
  return toMe(res.data);
}

export async function getSavedRecipes() {
  if (USE_MOCK) { await wait(); return clone(db.recipes.filter((r) => r.isBookmarked)); }
  const res = await client.get("/api/users/me/bookmarks", { params: { size: 100 } });
  return res.data.content.map(toCard);
}

export async function deleteAccount() {
  if (USE_MOCK) { await wait(300); return; }
  await client.delete("/api/users/me");
}
