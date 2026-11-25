import client from "./client";
import { USE_MOCK } from "./recipes";

export async function login(email, password) {
  if (USE_MOCK) {
    if (!email || !password) throw { message: "이메일과 비밀번호를 입력해 주세요." };
    return { accessToken: "mock-access", refreshToken: "mock-refresh" };
  }
  const res = await client.post("/api/auth/login", { email, password });
  return res.data; // { accessToken, refreshToken, ... }
}

// 서버는 토큰을 저장하지 않으므로, 프론트에서 토큰을 지우면 로그아웃이다.
export async function logout() {
  if (!USE_MOCK) {
    try { await client.post("/api/auth/logout"); } catch { /* 실패해도 로컬 로그아웃은 진행 */ }
  }
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
}

// 회원가입. 서버는 가입 즉시 토큰을 돌려준다. (요청 필드명은 서버와 맞는지 확인: email, password, nickname)
export async function signup({ email, password, nickname }) {
  if (USE_MOCK) {
    await new Promise((r) => setTimeout(r, 300));
    return { accessToken: "mock-access", refreshToken: "mock-refresh" };
  }
  const res = await client.post("/api/auth/signup", { email, password, nickname });
  return res.data;
}
