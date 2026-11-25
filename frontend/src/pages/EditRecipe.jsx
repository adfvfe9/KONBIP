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
    items: (recipe.items ?? []).map((i) => ({ productId: i.productId, name: i.name, brand: i.brand, quantity: i.quantity })),
    steps: recipe.steps,
  };

  return (
    <RecipeForm
      mode="edit"
      initial={initial}
      onSubmit={async (data, file) => {
        // 화면에 입력칸이 없는 설명은 기존 값을 그대로 보낸다. (안 보내면 지워짐)
        await updateRecipe(id, data, file, { description: recipe.description });
        navigate(`/my-recipes/${id}`, { replace: true });
      }}
      onDelete={async () => {
        await deleteRecipe(id);
        navigate("/my-recipes", { replace: true });
      }}
    />
  );
}
