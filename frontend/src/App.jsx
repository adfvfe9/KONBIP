import { Routes, Route } from "react-router-dom";
import TabLayout from "./layouts/TabLayout";
import Splash from "./pages/Splash";
import Login from "./pages/Login";
import Home from "./pages/Home";
import Search from "./pages/Search";
import SearchResults from "./pages/SearchResults";
import RecipeDetail from "./pages/RecipeDetail";
import CreateRecipe from "./pages/CreateRecipe";
import MyRecipes from "./pages/MyRecipes";
import EditRecipe from "./pages/EditRecipe";
import MyPage from "./pages/MyPage";
import SavedRecipes from "./pages/SavedRecipes";
import ProfileEdit from "./pages/ProfileEdit";
import Placeholder from "./pages/Placeholder";

export default function App() {
  return (
    <div className="app">
      <Routes>
        <Route path="/" element={<Splash />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Placeholder title="회원가입" />} />
        <Route path="/search" element={<Search />} />
        <Route path="/search/results" element={<SearchResults />} />
        <Route path="/recipes/:id" element={<RecipeDetail />} />

        {/* 조합 만들기 */}
        <Route path="/create" element={<CreateRecipe />} />
        <Route path="/my-recipes" element={<MyRecipes />} />
        <Route path="/my-recipes/:id" element={<RecipeDetail mine />} />
        <Route path="/my-recipes/:id/edit" element={<EditRecipe />} />

        {/* 마이페이지 하위 화면 (하단 탭 없음) */}
        <Route path="/mypage/saved" element={<SavedRecipes />} />
        <Route path="/mypage/profile" element={<ProfileEdit />} />
        <Route path="/mypage/password" element={<Placeholder title="비밀번호 변경" />} />

        {/* 하단 탭이 있는 화면 */}
        <Route element={<TabLayout />}>
          <Route path="/home" element={<Home />} />
          <Route path="/mypage" element={<MyPage />} />
        </Route>
      </Routes>
    </div>
  );
}
