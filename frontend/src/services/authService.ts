import { Bird } from "../types/Bird";

const API_BASE = "http://localhost:8080/api/birds";

function normalizeBird(bird: any): Bird {
  return {
    ...bird,
    _id: typeof bird._id === "object" ? bird._id.$oid : bird._id,
    id: typeof bird._id === "object" ? bird._id.$oid : bird._id, // legacy safety
  };
}

export async function fetchAllBirds(): Promise<Bird[]> {
  const res = await fetch(API_BASE);

  if (!res.ok) {
    throw new Error("Failed to fetch birds");
  }

  const data = await res.json();
  return data.map(normalizeBird);
}

export async function fetchBirdById(id: string): Promise<Bird> {
  const res = await fetch(`${API_BASE}/${id}`);

  if (!res.ok) {
    throw new Error("Failed to fetch bird");
  }

  return normalizeBird(await res.json());
}
