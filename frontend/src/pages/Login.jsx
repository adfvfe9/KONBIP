import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login, saveTokens } from "../api/auth";

export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [noMascot, setNoMascot] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      saveTokens(await login(email.trim(), password));
      navigate("/home", { replace: true });
    } catch (err) {
      setError(err?.message ?? "로그인에 실패했어요. 다시 시도해 주세요.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login">
      <span className="logo">KONBIP</span>
      <p className="login__tag">BIP Your Combo!</p>

      <form onSubmit={handleSubmit}>
        <input
          className="field" type="email" placeholder="이메일을 입력해주세요"
          value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email"
        />
        <input
          className="field" type="password" placeholder="비밀번호를 입력해주세요"
          value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password"
        />
        {error && <p className="error">{error}</p>}
        <button className="btn" type="submit" disabled={loading}>로그인하기</button>
      </form>

      <div className="login__links">
        <Link to="/signup">회원가입</Link>
        <Link to="/find-password">비밀번호 찾기</Link>
      </div>

      {/* 소셜 로그인은 백엔드에 OAuth 키가 등록된 뒤에 연결합니다. */}
      <div className="login__social">
        <button type="button" className="social social--google" aria-label="Google로 로그인">
          <svg width="22" height="22" viewBox="0 0 48 48" aria-hidden="true">
            <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z" />
            <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z" />
            <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z" />
            <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z" />
          </svg>
        </button>
        <button type="button" className="social social--kakao" aria-label="카카오로 로그인">
          <svg width="26" height="26" viewBox="0 0 24 24" aria-hidden="true">
            <ellipse cx="12" cy="11" rx="10" ry="7.5" fill="#3a1d1d" />
            <path d="M6 16.5l-1.5 4 5-2.5z" fill="#3a1d1d" />
            <text x="12" y="13" textAnchor="middle" fontSize="5.2" fontWeight="800" fill="#fee500" fontFamily="Pretendard, sans-serif">TALK</text>
          </svg>
        </button>
      </div>

      {/* 캐릭터 이미지: frontend/public/icon.png */}
      {!noMascot && <img className="login__mascot" src="/icon.png" alt="" onError={() => setNoMascot(true)} />}
    </div>
  );
}
