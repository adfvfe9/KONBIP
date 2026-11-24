import { Outlet } from "react-router-dom";
import BottomTab from "../components/BottomTab";

export default function TabLayout() {
  return (
    <>
      <Outlet />
      <BottomTab />
    </>
  );
}
