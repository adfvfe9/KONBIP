import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { IconHome, IconPlus, IconUser } from "./Icons";

export default function BottomTab() {
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  function go(path) {
    setMenuOpen(false);
    navigate(path);
  }

  return (
    <>
      {menuOpen && (
        <div className="overlay" onClick={() => setMenuOpen(false)}>
          <div className="menu" onClick={(e) => e.stopPropagation()}>
            <button onClick={() => go("/create")}>내 조합 추가하기</button>
            <button onClick={() => go("/my-recipes")}>내 조합 보기</button>
          </div>
        </div>
      )}

      <nav className="tabbar">
        <NavLink to="/home" className={({ isActive }) => (isActive && !menuOpen ? "active" : "")} onClick={() => setMenuOpen(false)}>
          <IconHome /><span>홈</span>
        </NavLink>

        {/* 가운데 탭은 이동하지 않고 메뉴를 연다 */}
        <button className={menuOpen ? "active" : ""} onClick={() => setMenuOpen((v) => !v)}>
          <IconPlus /><span>조합 만들기</span>
        </button>

        <NavLink to="/mypage" className={({ isActive }) => (isActive && !menuOpen ? "active" : "")} onClick={() => setMenuOpen(false)}>
          <IconUser /><span>마이페이지</span>
        </NavLink>
      </nav>
    </>
  );
}
