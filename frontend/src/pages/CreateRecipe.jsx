import { useNavigate } from "react-router-dom";
import RecipeForm from "../components/RecipeForm";
import { createRecipe } from "../api/recipes";

export default function CreateRecipe() {
  const navigate = useNavigate();
  return (
    <RecipeForm
      mode="create"
      onSubmit={async (data, file) => {
        await createRecipe(data, file);
        navigate("/home", { replace: true });
      }}
    />
  );
}
