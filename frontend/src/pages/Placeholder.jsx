import { useNavigate } from "react-router-dom";
import { IconBack } from "../components/Icons";

export default function Placeholder({ title }) {
  const navigate = useNavigate();
  return (
    <main className="page page--plain" style={{ paddingTop: 24 }}>
      <button className="back" onClick={() => navigate(-1)} aria-label="뒤로"><IconBack /></button>
      <h1 style={{ fontSize: 18, marginTop: 16 }}>{title}</h1>
      <p className="empty">아직 만들지 않은 화면이에요.</p>
    </main>
  );
}
