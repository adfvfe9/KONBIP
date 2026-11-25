import { useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import Logo from "./Logo";
import Modal from "./Modal";
import ProductPicker from "./ProductPicker";
import { IconBack, IconPlus } from "./Icons";
import { imgUrl } from "../api/client";

const BRANDS = ["세븐일레븐", "패밀리마트", "로손"];

// "#매콤 #반숙란, 장조림" → ["매콤", "반숙란", "장조림"]
const parseTags = (text) => [...new Set(text.split(/[\s,#]+/).map((t) => t.trim()).filter(Boolean))];

/**
 * 조합 등록·수정 공용 폼
 * mode: "create" | "edit"
 * initial: { title, tags[], brand, items[{ productId, name, brand, quantity }], steps[], imageUrl }
 * onSubmit(data, file), onDelete()  (onDelete 는 edit 일 때만)
 */
export default function RecipeForm({ mode = "create", initial = {}, onSubmit, onDelete }) {
  const navigate = useNavigate();
  const fileRef = useRef(null);
  const isEdit = mode === "edit";

  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(initial.imageUrl ? imgUrl(initial.imageUrl) : null);
  const [title, setTitle] = useState(initial.title ?? "");
  const [tagText, setTagText] = useState((initial.tags ?? []).map((t) => `#${t}`).join(" "));
  const [brand, setBrand] = useState(initial.brand ?? "");
  const [customBrand, setCustomBrand] = useState(!!initial.brand && !BRANDS.includes(initial.brand));
  const [items, setItems] = useState(initial.items ?? []);
  const [steps, setSteps] = useState(initial.steps?.length ? initial.steps : [""]);
  const [modal, setModal] = useState(null); // { title, message }
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [saving, setSaving] = useState(false);

  function pickFile(e) {
    const f = e.target.files?.[0];
    if (!f) return;
    if (!/^image\/(jpeg|png)$/.test(f.type) || f.size > 5 * 1024 * 1024) {
      setModal({ title: "사진을 추가할 수 없어요!", message: "jpg, png 파일만 올릴 수 있고 5MB 이하여야 해요." });
      return;
    }
    setFile(f);
    setPreview(URL.createObjectURL(f));
  }

  const setStep = (i, v) => setSteps((s) => s.map((x, idx) => (idx === i ? v : x)));

  async function submit() {
    const cleanSteps = steps.map((s) => s.trim()).filter(Boolean);

    // 수정할 때는 이미 올라간 사진이 있다고 보고, 새로 등록할 때만 사진을 필수로 확인한다.
    if (!preview && !isEdit) return setModal({ title: "사진 항목이 추가 되지 않았어요!", message: "항목을 다시 확인하고 추가해 주세요." });
    if (!title.trim()) return setModal({ title: "조합 이름이 비어 있어요!", message: "조합 이름을 작성해 주세요." });
    if (!brand.trim()) return setModal({ title: "대표 편의점이 선택되지 않았어요!", message: "대표 편의점을 골라 주세요." });
    if (items.length === 0) return setModal({ title: "조합 재료가 비어 있어요!", message: "상품을 검색해서 1개 이상 담아 주세요." });
    if (cleanSteps.length === 0) return setModal({ title: "레시피가 비어 있어요!", message: "조리 순서를 한 단계 이상 적어 주세요." });

    setSaving(true);
    try {
      await onSubmit({ title: title.trim(), items, brand: brand.trim(), tags: parseTags(tagText), steps: cleanSteps }, file);
    } catch (err) {
      setModal({ title: isEdit ? "수정에 실패했어요!" : "등록에 실패했어요!", message: err?.message ?? "잠시 후 다시 시도해 주세요." });
      setSaving(false);
    }
  }

  async function remove() {
    try { await onDelete(); }
    catch (err) {
      setConfirmDelete(false);
      setModal({ title: "삭제에 실패했어요!", message: err?.message ?? "잠시 후 다시 시도해 주세요." });
    }
  }

  return (
    <>
      <header className="header"><Logo /></header>
      <main className="page page--plain">
        <div className="titlebar">
          <button className="back" onClick={() => navigate(-1)} aria-label="뒤로"><IconBack /></button>
          <h1>{isEdit ? "수정하기" : "조합 등록하기"}</h1>
          {isEdit && <button className="titlebar__action titlebar__action--red" onClick={() => setConfirmDelete(true)}>삭제하기</button>}
        </div>

        <div className="create-top">
          <button type="button" className="photo" onClick={() => fileRef.current?.click()}
            style={preview ? { backgroundImage: `url(${preview})` } : undefined}>
            {!preview && (<><IconPlus width={34} height={34} strokeWidth={1} /><span>{isEdit ? "사진 수정" : "사진 추가"}</span></>)}
          </button>
          <input ref={fileRef} type="file" accept="image/jpeg,image/png" hidden onChange={pickFile} />

          <div className="create-top__inputs">
            <input className="line" placeholder="조합 이름을 작성해주세요" value={title} onChange={(e) => setTitle(e.target.value)} maxLength={50} />
            <input className="line" placeholder="해시태그 추가하기" value={tagText} onChange={(e) => setTagText(e.target.value)} />
          </div>
        </div>

        <h2 className="block-title">대표 편의점 등록</h2>
        <div className="chip-wrap">
          {BRANDS.map((b) => (
            <button key={b} type="button" className={`pill pill--lg${brand === b && !customBrand ? " on" : ""}`}
              onClick={() => { setBrand(b); setCustomBrand(false); }}>{b}</button>
          ))}
          <button type="button" className={`pill pill--lg${customBrand ? " on" : ""}`}
            onClick={() => { setCustomBrand(true); setBrand(customBrand ? brand : ""); }}>직접 추가하기</button>
        </div>
        {customBrand && (
          <input className="line" style={{ marginTop: 12 }} placeholder="편의점 이름을 입력해주세요"
            value={brand} onChange={(e) => setBrand(e.target.value)} />
        )}

        <h2 className="block-title" style={{ marginTop: 40 }}>조합 재료</h2>
        <ProductPicker value={items} onChange={setItems} />

        <h2 className="block-title" style={{ marginTop: 40 }}>레시피 작성</h2>
        <ol className="steps">
          {steps.map((s, i) => (
            <li key={i}>
              <span className="steps__no">{i + 1}.</span>
              <textarea className="line line--area" rows={1} value={s}
                placeholder={i === 0 ? "조합의 레시피를 순서대로 적어주세요" : ""}
                onChange={(e) => setStep(i, e.target.value)} />
            </li>
          ))}
        </ol>
        <button type="button" className="btn-outline" onClick={() => setSteps((s) => [...s, ""])}>단계 추가하기</button>

        <button type="button" className="btn btn--block" style={{ marginTop: 48 }} onClick={submit} disabled={saving}>
          {isEdit ? "레시피 수정하기" : "레시피 등록하기"}
        </button>
      </main>

      {modal && <Modal title={modal.title} message={modal.message} onClose={() => setModal(null)} />}
      {confirmDelete && (
        <Modal mascot="/icon3.png" fallback="😢" title="정말로 삭제하시겠어요?" onClose={() => setConfirmDelete(false)}>
          <button className="btn btn--danger btn--block" style={{ marginTop: 18 }} onClick={remove}>삭제하기</button>
        </Modal>
      )}
    </>
  );
}
