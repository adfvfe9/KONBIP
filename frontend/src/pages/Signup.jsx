import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { signup } from "../api/auth";

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function Signup() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "", confirm: "", nickname: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  function validate() {
    if (!EMAIL_RE.test(form.email.trim())) return "이메일 형식이 올바르지 않아요.";
    if (form.password.length < 8) return "비밀번호는 8자 이상 입력해 주세요.";
    if (form.password !== form.confirm) return "비밀번호가 서로 달라요.";
    if (!form.nickname.trim()) return "닉네임을 입력해 주세요.";
    return "";
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const message = validate();
    if (message) return setError(message);

    setError("");
    setLoading(true);
    try {
      const tokens = await signup({ email: form.email.trim(), password: form.password, nickname: form.nickname.trim() });
      localStorage.setItem("accessToken", tokens.accessToken);
      localStorage.setItem("refreshToken", tokens.refreshToken);
      navigate("/home", { replace: true }); // 가입하면 바로 로그인된 상태가 된다
    } catch (err) {
      // 서버 오류 코드(예: 이메일·닉네임 중복)는 message 로 안내한다
      setError(err?.message ?? "회원가입에 실패했어요. 잠시 후 다시 시도해 주세요.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login login--signup">
      <span className="logo">KONBIP</span>
      <p className="login__tag">회원가입</p>

      <form onSubmit={handleSubmit}>
        <input className="field" type="email" placeholder="이메일을 입력해주세요" value={form.email} onChange={set("email")} autoComplete="email" />
        <input className="field" type="text" placeholder="닉네임을 입력해주세요" value={form.nickname} onChange={set("nickname")} maxLength={20} autoComplete="nickname" />
        <input className="field" type="password" placeholder="비밀번호를 입력해주세요 (8자 이상)" value={form.password} onChange={set("password")} autoComplete="new-password" />
        <input className="field" type="password" placeholder="비밀번호를 한 번 더 입력해주세요" value={form.confirm} onChange={set("confirm")} autoComplete="new-password" />
        {error && <p className="error">{error}</p>}
        <button className="btn" type="submit" disabled={loading}>회원가입하기</button>
      </form>

      <div className="login__links">
        <Link to="/login">이미 계정이 있어요 · 로그인</Link>
      </div>
    </div>
  );
}
