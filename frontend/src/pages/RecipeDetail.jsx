import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Logo from "../components/Logo";
import RecipeSheet from "../components/RecipeSheet";
import { IconBack, IconStar } from "../components/Icons";
import { imgUrl } from "../api/client";
import { getRecipe, getReviews, addReview, toggleBookmark } from "../api/recipes";

const Stars = ({ value }) => (
  <span className="stars" aria-label={`별점 ${value}점`}>{"★".repeat(value)}{"☆".repeat(5 - value)}</span>
);

// mine=true 이면 '내 조합' 상세 (제목 + 수정하기 링크)
export default function RecipeDetail({ mine = false }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const [recipe, setRecipe] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [sort, setSort] = useState("latest");
  const [sheet, setSheet] = useState(false);
  const [text, setText] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    getRecipe(id).then(setRecipe).catch((e) => setError(e?.message ?? "불러오지 못했어요."));
    getReviews(id).then(setReviews);
  }, [id]);

  if (error) return <p className="empty">{error}</p>;
  if (!recipe) return <p className="empty">불러오는 중...</p>;

  const sorted = [...reviews].sort((a, b) => (sort === "popular" ? b.likeCount - a.likeCount : b.createdAt - a.createdAt));

  async function bookmark() {
    const r = await toggleBookmark(id, !recipe.isBookmarked);
    setRecipe((p) => ({ ...p, ...r }));
  }

  async function submitReview(e) {
    e.preventDefault();
    const content = text.trim();
    if (!content) return;
    const r = await addReview(id, content);
    setReviews((list) => [r, ...list]);
    setText("");
  }

  // 후기 좋아요는 서버 API가 없어서 화면에서만 바뀐다.
  const toggleLike = (rid) =>
    setReviews((list) => list.map((r) => (r.id === rid ? { ...r, liked: !r.liked, likeCount: r.likeCount + (r.liked ? -1 : 1) } : r)));

  const hero = recipe.imageUrl ? imgUrl(recipe.imageUrl) : null;

  return (
    <>
      <header className="header"><Logo /></header>
      <main className="page page--plain detail">
        <div className="titlebar titlebar--tight">
          <button className="back" onClick={() => navigate(-1)} aria-label="뒤로"><IconBack /></button>
          {mine && <h1>내 조합</h1>}
          {mine && <Link className="titlebar__action" to={`/my-recipes/${id}/edit`}>수정하기</Link>}
        </div>

        <div className="hero" style={hero ? { backgroundImage: `url(${hero})` } : undefined} />

        <h2 className="detail-title"><span className="card__brand">{recipe.brand}</span> {recipe.title}</h2>
        <div className="card__tags">{recipe.tags.map((t) => <span key={t}># {t}</span>)}</div>
        {recipe.description && <p className="detail-desc">{recipe.description}</p>}
        <hr className="rule" style={{ marginTop: 36 }} />

        <div className="review-head"><b>후기</b><span>{reviews.length}</span></div>
        <div className="pills" style={{ marginTop: 12 }}>
          {[["latest", "최신순"], ["popular", "인기순"]].map(([k, label]) => (
            <button key={k} className={`pill${sort === k ? " on" : ""}`} onClick={() => setSort(k)}>{label}</button>
          ))}
        </div>

        <form onSubmit={submitReview}>
          <input className="comment-input" placeholder="댓글 추가하기" value={text} onChange={(e) => setText(e.target.value)} />
        </form>

        {sorted.map((r) => (
          <article key={r.id} className="review">
            <div className="review__avatar" />
            <div className="review__main">
              <div className="review__who"><b>{r.nickname}</b><span>• {r.timeText}</span></div>
              <Stars value={r.rating} />
            </div>
            <button className="review__like" onClick={() => toggleLike(r.id)} aria-label="후기 좋아요">
              <span className={r.liked ? "heart heart--on" : "heart"}>{r.liked ? "♥" : "♡"}</span>
              <small>{r.likeCount}</small>
            </button>
            <p className="review__text">{r.content}</p>
          </article>
        ))}
      </main>

      <div className="actionbar">
        <button className="actionbar__star" onClick={bookmark} aria-label="즐겨찾기">
          <IconStar width={34} height={34} fill={recipe.isBookmarked ? "var(--orange)" : "none"} stroke="var(--orange)" />
          <span>{recipe.bookmarkCount}</span>
        </button>
        <button className="btn actionbar__btn" onClick={() => setSheet(true)}>레시피 확인하기</button>
      </div>

      {sheet && <RecipeSheet recipe={recipe} onClose={() => setSheet(false)} />}
    </>
  );
}
