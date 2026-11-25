import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { imgUrl } from "../api/client";

// 홈 상단 배너: 인기 꿀조합을 자동으로 넘기고, 좌우로 밀거나 점을 눌러 이동할 수 있다.
export default function Banner({ recipes = [], interval = 4000 }) {
  const [index, setIndex] = useState(0);
  const startX = useRef(null);
  const swiped = useRef(false);
  const count = recipes.length;

  useEffect(() => {
    if (count < 2) return;
    const t = setTimeout(() => setIndex((i) => (i + 1) % count), interval);
    return () => clearTimeout(t); // index 가 바뀔 때마다 다시 시작
  }, [index, count, interval]);

  if (count === 0) return <div className="banner" />;

  const go = (i) => setIndex((i + count) % count);

  function onTouchEnd(e) {
    if (startX.current === null) return;
    const dx = e.changedTouches[0].clientX - startX.current;
    startX.current = null;
    swiped.current = Math.abs(dx) > 40;
    if (swiped.current) go(index + (dx < 0 ? 1 : -1));
  }

  return (
    <div className="banner" onTouchStart={(e) => { startX.current = e.touches[0].clientX; swiped.current = false; }} onTouchEnd={onTouchEnd}>
      <div className="banner__track" style={{ transform: `translateX(-${index * 100}%)` }}>
        {recipes.map((r) => {
          const src = imgUrl(r.imageUrl);
          return (
            <Link key={r.id} to={`/recipes/${r.id}`} className="banner__slide"
              style={src ? { backgroundImage: `url(${src})` } : undefined}
              onClick={(e) => { if (swiped.current) e.preventDefault(); }}>
              <div className="banner__caption">
                <span>{r.brand}</span>
                <strong>{r.title}</strong>
              </div>
            </Link>
          );
        })}
      </div>

      <div className="banner__dots">
        {recipes.map((r, i) => (
          <button key={r.id} className={i === index ? "on" : ""} onClick={() => go(i)} aria-label={`${i + 1}번째 배너`} />
        ))}
      </div>
    </div>
  );
}
