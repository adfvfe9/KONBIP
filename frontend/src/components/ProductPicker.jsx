import { useEffect, useState } from "react";
import { searchProducts } from "../api/products";

const MAX_QUANTITY = 10; // 서버 규칙: 한 상품 최대 10개

/**
 * 조합 재료 고르기: 상품을 검색해서 담고, 수량을 정한다.
 * value: [{ productId, name, brand, quantity }]
 * onChange(nextValue)
 */
export default function ProductPicker({ value, onChange }) {
  const [keyword, setKeyword] = useState("");
  const [results, setResults] = useState([]);
  const [open, setOpen] = useState(false);
  const [error, setError] = useState("");

  // 입력이 멈추고 0.3초 뒤에 검색한다.
  useEffect(() => {
    if (!open) return;
    let cancelled = false;
    const t = setTimeout(() => {
      searchProducts(keyword)
        .then((list) => { if (!cancelled) { setResults(list); setError(""); } })
        .catch(() => { if (!cancelled) setError("상품을 불러오지 못했어요."); });
    }, 300);
    return () => { cancelled = true; clearTimeout(t); };
  }, [keyword, open]);

  const picked = new Set(value.map((p) => p.productId));

  function add(p) {
    if (picked.has(p.productId)) return;
    onChange([...value, { productId: p.productId, name: p.name, brand: p.brand, quantity: 1 }]);
  }

  const setQuantity = (id, q) =>
    onChange(value.map((p) => (p.productId === id ? { ...p, quantity: Math.min(MAX_QUANTITY, Math.max(1, q)) } : p)));
  const remove = (id) => onChange(value.filter((p) => p.productId !== id));

  return (
    <div className="picker">
      {value.length > 0 && (
        <ul className="picker__picked">
          {value.map((p) => (
            <li key={p.productId}>
              <span className="picker__name">
                {p.brand && <small>{p.brand}</small>}
                {p.name}
              </span>
              <span className="picker__qty">
                <button type="button" onClick={() => setQuantity(p.productId, p.quantity - 1)} disabled={p.quantity <= 1} aria-label="수량 빼기">−</button>
                <b>{p.quantity}</b>
                <button type="button" onClick={() => setQuantity(p.productId, p.quantity + 1)} disabled={p.quantity >= MAX_QUANTITY} aria-label="수량 더하기">+</button>
              </span>
              <button type="button" className="picker__remove" onClick={() => remove(p.productId)} aria-label={`${p.name} 빼기`}>×</button>
            </li>
          ))}
        </ul>
      )}

      <input className="line" placeholder="재료 상품을 검색해 담아주세요" value={keyword}
        onChange={(e) => setKeyword(e.target.value)} onFocus={() => setOpen(true)} />

      {open && (
        <div className="picker__results">
          {error && <p className="picker__empty">{error}</p>}
          {!error && results.length === 0 && <p className="picker__empty">검색 결과가 없어요.</p>}
          {results.map((p) => {
            const on = picked.has(p.productId);
            return (
              <button type="button" key={p.productId} className={`picker__item${on ? " on" : ""}`} onClick={() => add(p)} disabled={on}>
                <span className="picker__name">
                  <small>{p.brand}{p.category ? ` · ${p.category}` : ""}</small>
                  {p.name}
                </span>
                <span className="picker__add">{on ? "담김" : "담기"}</span>
              </button>
            );
          })}
          <button type="button" className="picker__close" onClick={() => setOpen(false)}>닫기</button>
        </div>
      )}
    </div>
  );
}
