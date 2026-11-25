import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Logo from "../components/Logo";
import RecipeCard from "../components/RecipeCard";
import { IconBack } from "../components/Icons";
import { getSavedRecipes } from "../api/users";

export default function SavedRecipes() {
  const navigate = useNavigate();
  const [list, setList] = useState(null);

  useEffect(() => { getSavedRecipes().then(setList); }, []);

  return (
    <>
      <header className="header"><Logo /></header>
      <main className="page page--plain">
        <div className="titlebar">
          <button className="back" onClick={() => navigate("/mypage")} aria-label="뒤로"><IconBack /></button>
          <h1>저장된 조합</h1>
        </div>
        {list === null && <p className="empty">불러오는 중...</p>}
        {list?.length === 0 && <p className="empty">저장한 조합이 없어요.<br />마음에 드는 조합의 별을 눌러 보세요.</p>}
        {list?.map((r) => <RecipeCard key={r.id} recipe={r} />)}
      </main>
    </>
  );
}
