import axios from "axios";

export const API = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

// 서버가 준 상대 경로(/api/recipes/1/image)를 전체 주소로 바꾼다. null 이면 null.
export const imgUrl = (path) => {
  if (!path) return null;
  // 서버가 주는 상대 경로(/api/...)만 서버 주소를 붙이고, public 폴더 이미지(/abc.jpeg)와 외부 주소는 그대로 쓴다.
  return path.startsWith("/api/") ? API + path : path;
};

const client = axios.create({ baseURL: API });

client.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

client.interceptors.response.use(
  (res) => res.data, // { success, message, data }
  async (err) => {
    const original = err.config;
    const body = err.response?.data;

    if (err.response?.status === 401 && body?.code === "INVALID_TOKEN" && !original._retry) {
      original._retry = true;
      const refreshToken = localStorage.getItem("refreshToken");
      if (refreshToken) {
        try {
          const { data } = await axios.post(`${API}/api/auth/refresh`, { refreshToken });
          localStorage.setItem("accessToken", data.data.accessToken);
          if (data.data.refreshToken) localStorage.setItem("refreshToken", data.data.refreshToken);
          return client(original);
        } catch {
          /* 갱신 실패 → 아래에서 로그아웃 */
        }
      }
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      window.location.href = "/login";
    }

    // 로그인이 필요한 기능(즐겨찾기, 댓글, 마이페이지 등)을 비회원이 쓰면 로그인 화면으로 보낸다.
    if (err.response?.status === 401 && body?.code === "UNAUTHORIZED" && window.location.pathname !== "/login") {
      window.location.href = "/login";
    }
    return Promise.reject(body ?? err);
  }
);

export default client;
