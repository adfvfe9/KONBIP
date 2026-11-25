import { useState } from "react";

// mascot: public 폴더에 넣은 캐릭터 이미지 주소. 이미지가 없으면 fallback 이모지를 보여 준다.
export default function Modal({ title, message, onClose, mascot = "/icon2.png", fallback = "🍜", children }) {
  const [broken, setBroken] = useState(false);
  return (
    <div className="overlay overlay--center" onClick={onClose}>
      <div className="modal" role="dialog" aria-modal="true" onClick={(e) => e.stopPropagation()}>
        <button className="modal__close" onClick={onClose} aria-label="닫기">✕</button>
        <div className="modal__mascot">
          {broken ? <span aria-hidden="true">{fallback}</span> : <img src={mascot} alt="" onError={() => setBroken(true)} />}
        </div>
        <p className="modal__title">{title}</p>
        {message && <p className="modal__msg">{message}</p>}
        {children}
      </div>
    </div>
  );
}
