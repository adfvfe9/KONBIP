import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Logo from "../components/Logo";
import Modal from "../components/Modal";
import { IconBack, IconCamera, IconPencil, IconUser } from "../components/Icons";
import { imgUrl } from "../api/client";
import { getMe, updateNickname, updateProfileImage } from "../api/users";

export default function ProfileEdit() {
  const navigate = useNavigate();
  const fileRef = useRef(null);
  const [me, setMe] = useState(null);
  const [editing, setEditing] = useState(false);
  const [nickname, setNickname] = useState("");
  const [modal, setModal] = useState(null);

  useEffect(() => { getMe().then((m) => { setMe(m); setNickname(m.nickname); }); }, []);

  async function saveNickname() {
    const value = nickname.trim();
    setEditing(false);
    if (!value || value === me.nickname) return setNickname(me.nickname);
    try {
      setMe(await updateNickname(value));
    } catch (e) {
      setNickname(me.nickname);
      setModal({ title: "닉네임을 바꿀 수 없어요!", message: e?.message ?? "이미 사용 중인 닉네임일 수 있어요." });
    }
  }

  async function pickPhoto(e) {
    const f = e.target.files?.[0];
    if (!f) return;
    if (!/^image\/(jpeg|png)$/.test(f.type) || f.size > 5 * 1024 * 1024) {
      return setModal({ title: "사진을 추가할 수 없어요!", message: "jpg, png 파일만 올릴 수 있고 5MB 이하여야 해요." });
    }
    try {
      setMe(await updateProfileImage(f));
    } catch (err) {
      setModal({ title: "사진을 바꾸지 못했어요!", message: err?.message ?? "잠시 후 다시 시도해 주세요." });
    }
  }

  if (!me) return <p className="empty">불러오는 중...</p>;
  const photo = me.profileImageUrl ? imgUrl(me.profileImageUrl) : null;

  return (
    <>
      <header className="header"><Logo /></header>
      <main className="page page--plain">
        <div className="titlebar">
          <button className="back" onClick={() => navigate("/mypage")} aria-label="뒤로"><IconBack /></button>
          <h1>프로필 편집</h1>
        </div>

        <section className="profile" style={{ paddingTop: 20 }}>
          <button className="avatar avatar--edit" onClick={() => fileRef.current?.click()} aria-label="프로필 사진 변경"
            style={photo ? { backgroundImage: `url(${photo})` } : undefined}>
            {!photo && <IconCamera width={22} height={22} />}
          </button>
          <input ref={fileRef} type="file" accept="image/jpeg,image/png" hidden onChange={pickPhoto} />

          {editing ? (
            <input className="nick-input" value={nickname} maxLength={20} autoFocus
              onChange={(e) => setNickname(e.target.value)} onBlur={saveNickname}
              onKeyDown={(e) => e.key === "Enter" && e.currentTarget.blur()} />
          ) : (
            <button className="nick" onClick={() => setEditing(true)}>
              <IconPencil width={18} height={18} /><b>{me.nickname}</b>
            </button>
          )}
        </section>

        <Link to="/mypage/password" className="menu-row" style={{ marginTop: 40 }}>
          <IconUser width={22} height={22} stroke="var(--orange)" />
          <span>비밀번호 변경하기</span>
        </Link>
      </main>
      {modal && <Modal title={modal.title} message={modal.message} onClose={() => setModal(null)} />}
    </>
  );
}
