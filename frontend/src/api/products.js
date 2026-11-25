import client from "./client";
import { db } from "../mocks/recipes";
import { USE_MOCK } from "./recipes";

// 조합에 담을 상품 검색. → [{ productId, name, brand, category, price, calories, ... }]
export async function searchProducts(keyword, size = 20) {
  if (USE_MOCK) {
    const k = keyword.trim();
    return db.products.filter((p) => !k || p.name.includes(k) || p.brand.includes(k)).slice(0, size);
  }
  const res = await client.get("/api/products", { params: { keyword: keyword.trim() || undefined, size } });
  return res.data.content;
}
