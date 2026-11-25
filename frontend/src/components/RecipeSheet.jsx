import { useNavigate } from "react-router-dom";

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
          {steps.map((text, i) => (
            <div key={i} className={`step-row ${i % 2 ? "step-row--right" : ""}`}>
              <div className="step-mascot" aria-hidden="true">{i % 2 ? "🍜" : "🍙"}</div>
              <div className="step-card">
                <b>STEP {i + 1}</b>
                <p>{text}</p>
              </div>
            </div>
          ))}
        </div>

        <div className="sheet__done">
          <div className="step-mascot step-mascot--big" aria-hidden="true">🍙🍜</div>
          <mark>조합 준비 완료!</mark>
          <p>이제 맛있게 즐길 시간이에요!</p>
        </div>
        <button className="btn btn--block" onClick={() => navigate("/home")}>다른 레시피 확인하기</button>
      </div>
    </div>
  );
}
