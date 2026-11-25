import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Logo from "../components/Logo";
import SearchBar from "../components/SearchBar";
import { IconBack } from "../components/Icons";

const KEY = "recentSearches";
const load = () => { try { return JSON.parse(localStorage.getItem(KEY)) ?? []; } catch { return []; } };

export function saveRecent(keyword) {
  const next = [keyword, ...load().filter((k) => k !== keyword)].slice(0, 10);
  localStorage.setItem(KEY, JSON.stringify(next));
}

export default function Search() {
  const navigate = useNavigate();
  const [value, setValue] = useState("");
  const [recent, setRecent] = useState(load);

  function go(keyword) {
    const k = keyword.trim();
    if (!k) return;
    saveRecent(k);
    navigate(`/search/results?q=${encodeURIComponent(k)}`);
  }

  function clearAll() {
    localStorage.removeItem(KEY);
    setRecent([]);
  }

  return (
    <>
      <header className="header"><Logo /></header>
      <main className="page page--plain">
        <div className="searchrow">
          <button className="back" onClick={() => navigate(-1)} aria-label="뒤로"><IconBack /></button>
          <SearchBar value={value} onChange={setValue} onSubmit={go} autoFocus />
          <button className="cancel" onClick={() => navigate(-1)}>취소</button>
        </div>

        <div className="recent-head">
          <span>최근 검색기록</span>
          {recent.length > 0 && <button onClick={clearAll}>전체 삭제</button>}
        </div>
        {recent.length === 0 ? (
          <p className="empty">최근 검색한 꿀조합이 없어요.</p>
        ) : (
          <div className="chips">
            {recent.map((k) => (
              <button key={k} className="chip" onClick={() => go(k)}>{k}</button>
            ))}
          </div>
        )}
      </main>
    </>
  );
}
