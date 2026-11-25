import { useNavigate } from "react-router-dom";
import RecipeForm from "../components/RecipeForm";
import { createRecipe } from "../api/recipes";

export default function CreateRecipe() {
  const navigate = useNavigate();
  return (
    <RecipeForm
      mode="create"
      onSubmit={async (data, file) => {
        const created = await createRecipe(data, file);
        navigate(`/my-recipes/${created.id}`, { replace: true }); // 방금 만든 조합을 바로 보여 준다
      }}
    />
  );
}
