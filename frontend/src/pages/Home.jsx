import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Logo from "../components/Logo";
import SearchBar from "../components/SearchBar";
import RecipeCard from "../components/RecipeCard";
import Banner from "../components/Banner";
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

        {/* 배너: 인기 꿀조합 3개를 슬라이드로 보여 준다 */}
        <Banner recipes={home?.popular?.slice(0, 3)} />

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
