import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Logo from "../components/Logo";
import Modal from "../components/Modal";
import { IconStar } from "../components/Icons";
import { imgUrl } from "../api/client";
import { getMe, deleteAccount } from "../api/users";
import { logout } from "../api/auth";

export default function MyPage() {
  const navigate = useNavigate();
  const [me, setMe] = useState(null);
  const [confirm, setConfirm] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => { getMe().then(setMe); }, []);

  async function handleLogout() {
    await logout();
    navigate("/login", { replace: true });
  }

  async function handleWithdraw() {
    try {
      await deleteAccount();
      localStorage.clear();
      navigate("/login", { replace: true });
    } catch (e) {
      setConfirm(false);
      setError(e?.message ?? "탈퇴에 실패했어요. 잠시 후 다시 시도해 주세요.");
    }
  }

  const photo = me?.profileImageUrl ? imgUrl(me.profileImageUrl) : null;

  return (
    <>
      <header className="header"><Logo /></header>
      <main className="page">
        <section className="profile">
          <div className="avatar" style={photo ? { backgroundImage: `url(${photo})` } : undefined} />
          <p className="profile__name">{me?.nickname ?? " "}</p>
          <Link to="/mypage/profile" className="profile__edit">프로필 편집하기</Link>
        </section>
        <hr className="rule" style={{ margin: "28px 0 24px" }} />

        <Link to="/mypage/saved" className="menu-row">
          <IconStar width={22} height={22} stroke="var(--orange)" />
          <span>저장한 조합 보기</span>
        </Link>

        <div className="account">
          <button onClick={handleLogout}>로그아웃</button>
          <button className="account__danger" onClick={() => setConfirm(true)}>회원탈퇴</button>
        </div>
        {error && <p className="error" style={{ textAlign: "center" }}>{error}</p>}
      </main>

      {confirm && (
        <Modal mascot="/icon3.png" fallback="😢" title="정말로 탈퇴하시겠어요?"
          message="작성한 조합과 후기가 모두 삭제돼요." onClose={() => setConfirm(false)}>
          <button className="btn btn--danger btn--block" style={{ marginTop: 18 }} onClick={handleWithdraw}>탈퇴하기</button>
        </Modal>
      )}
    </>
  );
}
