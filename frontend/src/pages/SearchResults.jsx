import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import Logo from "../components/Logo";
import SearchBar from "../components/SearchBar";
import RecipeCard from "../components/RecipeCard";
import { IconBack } from "../components/Icons";
import { searchRecipes } from "../api/recipes";
import { saveRecent } from "./Search";

const SORTS = [
  { key: "latest", label: "최신순" },
  { key: "popular", label: "인기순" },
];

export default function SearchResults() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const q = params.get("q") ?? "";
  const [value, setValue] = useState(q);
  const [sort, setSort] = useState("latest");
  const [list, setList] = useState(null);

  useEffect(() => {
    setList(null);
    searchRecipes(q, sort).then(setList);
  }, [q, sort]);

  function submit(keyword) {
    const k = keyword.trim();
    if (!k) return;
    saveRecent(k);
    navigate(`/search/results?q=${encodeURIComponent(k)}`, { replace: true });
  }

  return (
    <>
      <header className="header"><Logo /></header>
      <main className="page page--plain">
        <div className="searchrow">
          <button className="back" onClick={() => navigate(-1)} aria-label="뒤로"><IconBack /></button>
          <SearchBar value={value} onChange={setValue} onSubmit={submit} />
        </div>

        <div className="pills">
          {SORTS.map((s) => (
            <button key={s.key} className={`pill${sort === s.key ? " on" : ""}`} onClick={() => setSort(s.key)}>
              {s.label}
            </button>
          ))}
        </div>

        {list === null && <p className="empty">불러오는 중...</p>}
        {list?.length === 0 && <p className="empty">‘{q}’에 맞는 꿀조합이 없어요. 다른 검색어로 찾아보세요.</p>}
        {list?.map((r) => <RecipeCard key={r.id} recipe={r} />)}
      </main>
    </>
  );
}
