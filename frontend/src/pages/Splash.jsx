import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

export default function Splash() {
  const navigate = useNavigate();

  useEffect(() => {
    const t = setTimeout(() => {
      navigate(localStorage.getItem("accessToken") ? "/home" : "/login", { replace: true });
    }, 1500);
    return () => clearTimeout(t);
  }, [navigate]);

  return (
    <div className="splash">
      <span className="logo">KONBIP</span>
      <p>BIP Your Combo!</p>
    </div>
  );
}
