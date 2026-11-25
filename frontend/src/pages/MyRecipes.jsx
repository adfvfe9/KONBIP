import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Logo from "../components/Logo";
import RecipeCard from "../components/RecipeCard";
import { IconBack } from "../components/Icons";
import { getMyRecipes } from "../api/recipes";

export default function MyRecipes() {
  const navigate = useNavigate();
  const [list, setList] = useState(null);

  useEffect(() => { getMyRecipes().then(setList); }, []);

  return (
    <>
      <header className="header"><Logo /></header>
      <main className="page page--plain">
        <div className="titlebar">
          <button className="back" onClick={() => navigate("/home")} aria-label="뒤로"><IconBack /></button>
          <h1>내 조합</h1>
        </div>

        {list === null && <p className="empty">불러오는 중...</p>}
        {list?.length === 0 && (
          <div className="empty">
            <p>아직 등록한 조합이 없어요.</p>
            <button className="btn" style={{ marginTop: 16, padding: "0 24px" }} onClick={() => navigate("/create")}>첫 조합 만들기</button>
          </div>
        )}
        {list?.map((r) => <RecipeCard key={r.recipeId} recipe={r} to={`/my-recipes/${r.recipeId}`} />)}
      </main>
    </>
  );
}
