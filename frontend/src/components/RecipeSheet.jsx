import { useState } from "react";
import { useNavigate } from "react-router-dom";

// public 폴더의 캐릭터 이미지. 파일이 없으면 이모지로 대신 보여 준다.
// STEP 옆 캐릭터는 i.png, i1.png 를 번갈아 쓰고, 마지막 '조합 준비 완료' 캐릭터는 i2.png 를 쓴다.
const STEP_MASCOTS = [
  { src: "/i.png", fallback: "🍙" },
  { src: "/i1.png", fallback: "🍜" },
];
const DONE_MASCOT = { src: "/i2.png", fallback: "🍙🍜" };

function Mascot({ src, fallback }) {
  const [broken, setBroken] = useState(false);
  if (broken) return <span aria-hidden="true">{fallback}</span>;
  return <img src={src} alt="" onError={() => setBroken(true)} />;
}

// '레시피 확인하기' 를 누르면 올라오는 하단 시트 (Figma 29번)
export default function RecipeSheet({ recipe, onClose }) {
  const navigate = useNavigate();
  const { ingredients = [], steps = [] } = recipe;

  return (
    <div className="overlay overlay--bottom" onClick={onClose}>
      <div className="sheet" onClick={(e) => e.stopPropagation()}>
        <div className="sheet__handle" onClick={onClose} />

        <h2 className="block-title" style={{ marginTop: 18 }}>조합 재료</h2>
        <div className="ing-row">
          {ingredients.map((ing) => (
            <div key={ing.name} className="ing">
              <div className="ing__thumb" />
              <span>{ing.name}{ing.quantity > 1 && <span className="ing__qty">×{ing.quantity}</span>}</span>
            </div>
          ))}
        </div>
        <hr className="rule" />

        <div className="step-list">
          {steps.map((text, i) => {
            const m = STEP_MASCOTS[i % STEP_MASCOTS.length];
            return (
              <div key={i} className={`step-row ${i % 2 ? "step-row--right" : ""}`}>
                <div className="step-mascot"><Mascot src={m.src} fallback={m.fallback} /></div>
                <div className="step-card">
                  <b>STEP {i + 1}</b>
                  <p>{text}</p>
                </div>
              </div>
            );
          })}
        </div>

        <div className="sheet__done">
          <div className="step-mascot step-mascot--big"><Mascot src={DONE_MASCOT.src} fallback={DONE_MASCOT.fallback} /></div>
          <mark>조합 준비 완료!</mark>
          <p>이제 맛있게 즐길 시간이에요!</p>
        </div>
        <button className="btn btn--block" onClick={() => navigate("/home")}>다른 레시피 확인하기</button>
      </div>
    </div>
  );
}
