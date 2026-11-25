import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import RecipeForm from "../components/RecipeForm";
import { getRecipe, updateRecipe, deleteRecipe } from "../api/recipes";

export default function EditRecipe() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [recipe, setRecipe] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    getRecipe(id).then(setRecipe).catch((e) => setError(e?.message ?? "불러오지 못했어요."));
  }, [id]);

  if (error) return <p className="empty">{error}</p>;
  if (!recipe) return <p className="empty">불러오는 중...</p>;

  const initial = {
    title: recipe.title,
    tags: recipe.tags,
    brand: recipe.brand,
    imageUrl: recipe.imageUrl,
    ingredientsText: (recipe.ingredients ?? []).map((i) => i.name).join(", "),
    steps: recipe.steps,
  };

  return (
    <RecipeForm
      mode="edit"
      initial={initial}
      onSubmit={async (data, file) => {
        await updateRecipe(id, data, file);
        navigate(`/my-recipes/${id}`, { replace: true });
      }}
      onDelete={async () => {
        await deleteRecipe(id);
        navigate("/my-recipes", { replace: true });
      }}
    />
  );
}
