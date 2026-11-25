import client from "./client";
import { USE_MOCK } from "./recipes";
import { db, saveDb } from "../mocks/recipes";

export function saveTokens({ accessToken, refreshToken }) {
  localStorage.setItem("accessToken", accessToken);
  localStorage.setItem("refreshToken", refreshToken);
}

export async function login(email, password) {
  if (USE_MOCK) {
    if (!email || !password) throw { message: "이메일과 비밀번호를 입력해 주세요." };
    return { accessToken: "mock-access", refreshToken: "mock-refresh" };
  }
  const res = await client.post("/api/auth/login", { email, password });
  return res.data; // { accessToken, refreshToken, user, ... }
}

// 가입하면 바로 로그인 토큰이 온다.
export async function signup(email, password, nickname) {
  if (USE_MOCK) {
    db.me.nickname = nickname; // 테스트 모드: 가입한 닉네임으로 활동한다
    saveDb();
    return { accessToken: "mock-access", refreshToken: "mock-refresh" };
  }
  const res = await client.post("/api/auth/signup", { email, password, nickname });
  return res.data;
}

// 서버는 토큰을 저장하지 않으므로, 프론트에서 토큰을 지우면 로그아웃이다.
export async function logout() {
  if (!USE_MOCK) {
    try { await client.post("/api/auth/logout"); } catch { /* 실패해도 로컬 로그아웃은 진행 */ }
  }
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
}

