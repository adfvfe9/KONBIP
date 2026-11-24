import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Logo from "../components/Logo";
import SearchBar from "../components/SearchBar";
import RecipeCard from "../components/RecipeCard";
import { getHome } from "../api/recipes";

export default function Home() {
  const navigate = useNavigate();
  const [home, setHome] = useState(null);

  useEffect(() => { getHome().then(setHome); }, []);

  return (
    <>
      <header className="header"><Logo /></header>
      <main className="page">
        <SearchBar readOnly onClick={() => navigate("/search")} />

        {/* 배너 자리: 디자인 확정 후 이미지/슬라이드로 교체 */}
        <div className="banner"><i /><i /><i /></div>

        <h2 className="section-title">최근 인기상승한 <mark>편의점 꿀조합</mark></h2>
        {!home ? (
          <p className="empty">불러오는 중...</p>
        ) : (
          home.popular.slice(0, 3).map((r, i) => <RecipeCard key={r.id} recipe={r} rank={i + 1} />)
        )}
      </main>
    </>
  );
}
