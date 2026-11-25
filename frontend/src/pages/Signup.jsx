import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { signup, saveTokens } from "../api/auth";

// 서버 규칙과 같게 맞춘다. (SignupRequest)
const NICKNAME = /^[\p{L}\p{N}_]{2,20}$/u;

export default function Signup() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "", confirm: "", nickname: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  function validate() {
    if (!form.email.trim()) return "이메일을 입력해 주세요.";
    if (form.password.length < 8 || form.password.length > 64) return "비밀번호는 8~64자여야 해요.";
    if (form.password !== form.confirm) return "비밀번호가 서로 달라요.";
    if (!NICKNAME.test(form.nickname.trim())) return "닉네임은 2~20자의 한글, 영문, 숫자, _ 만 쓸 수 있어요.";
    return "";
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const message = validate();
    setError(message);
    if (message) return;

    setLoading(true);
    try {
      const tokens = await signup(form.email.trim(), form.password, form.nickname.trim());
      saveTokens(tokens);
      navigate("/home", { replace: true });
    } catch (err) {
      setError(err?.message ?? "가입에 실패했어요. 다시 시도해 주세요.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login">
      <span className="logo">KONBIP</span>
      <p className="login__tag">BIP Your Combo!</p>

      <form onSubmit={handleSubmit}>
        <input className="field" type="email" placeholder="이메일을 입력해주세요"
          value={form.email} onChange={set("email")} autoComplete="email" />
        <input className="field" type="password" placeholder="비밀번호 (8자 이상)"
          value={form.password} onChange={set("password")} autoComplete="new-password" />
        <input className="field" type="password" placeholder="비밀번호를 한 번 더 입력해주세요"
          value={form.confirm} onChange={set("confirm")} autoComplete="new-password" />
        <input className="field" placeholder="닉네임 (2~20자)" maxLength={20}
          value={form.nickname} onChange={set("nickname")} autoComplete="nickname" />
        {error && <p className="error">{error}</p>}
        <button className="btn" type="submit" disabled={loading}>가입하기</button>
      </form>

      <div className="login__links">
        <Link to="/login">이미 계정이 있어요</Link>
      </div>
    </div>
  );
}
