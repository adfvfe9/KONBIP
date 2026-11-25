import { Link } from "react-router-dom";
import { IconStar, IconComment } from "./Icons";
import { imgUrl } from "../api/client";

const medalColor = { 1: "var(--gold)", 2: "var(--silver)", 3: "var(--bronze)" };

// to: 눌렀을 때 이동할 주소 (기본은 레시피 상세)
export default function RecipeCard({ recipe, rank, to }) {
  const { recipeId, title, brand, tags = [], imageUrl, bookmarkCount = 0, commentCount = 0 } = recipe;
  const src = imageUrl ? imgUrl(imageUrl) : null;

  return (
    <Link to={to ?? `/recipes/${recipeId}`} className="card">
      <div className="card__thumb" style={src ? { backgroundImage: `url(${src})` } : undefined}>
        {rank && (
          <span className="card__rank" style={{ background: medalColor[rank] ?? "var(--gray-text)" }}>{rank}</span>
        )}
      </div>
      <div className="card__body">
        <div className="card__title">
          {brand && <span className="card__brand">{brand}</span>}
          <span>{title}</span>
        </div>
        <div className="card__tags">{tags.map((t) => <span key={t}># {t}</span>)}</div>
        <div className="card__meta">
          <span><IconStar width={18} height={18} />{bookmarkCount}개</span>
          <span><IconComment width={18} height={18} />{commentCount}개</span>
        </div>
      </div>
    </Link>
  );
}
