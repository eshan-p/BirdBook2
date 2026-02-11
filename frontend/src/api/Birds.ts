import { Bird } from "../types/Bird";

const API_BASE = "http://localhost:8080/birds";

/* =========================
   READ
========================= */

export async function getAllBirds(): Promise<Bird[]> {
  const response = await fetch(API_BASE);

  if (!response.ok) {
    throw new Error("Failed to fetch birds");
  }

  return response.json();
}

export async function getBirdById(id: string): Promise<Bird> {
  const response = await fetch(`${API_BASE}/${id}`);

  if (!response.ok) {
    throw new Error("Failed to fetch bird");
  }

  return response.json();
}

/* =========================
   SEARCH
========================= */

export async function searchBirds(query: string): Promise<Bird[]> {
  const response = await fetch(
    `${API_BASE}/search?query=${encodeURIComponent(query)}`
  );

  if (!response.ok) {
    throw new Error("Failed to search birds");
  }

  return response.json();
}

/* =========================
   CREATE
========================= */

export async function addBird(
  bird: Partial<Bird>,
  imageFile?: File
): Promise<Bird> {
  const formData = new FormData();

  const birdData = {
    commonName: bird.commonName,
    scientificName: bird.scientificName,
    imageURL: bird.imageURL,
  };

  formData.append("bird", JSON.stringify(birdData));

  if (imageFile) {
    formData.append("image", imageFile);
  }

  const response = await fetch(API_BASE, {
    method: "POST",
    credentials: "include",
    body: formData,
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error("Failed to add bird");
  }

  return response.json();
}

/* =========================
   UPDATE
========================= */

export async function updateBird(
  id: string,
  bird: Partial<Bird>,
  imageFile?: File
): Promise<Bird> {
  const formData = new FormData();

  const birdData = {
    commonName: bird.commonName,
    scientificName: bird.scientificName,
    imageURL: bird.imageURL,
  };

  formData.append("bird", JSON.stringify(birdData));

  if (imageFile) {
    formData.append("image", imageFile);
  }

  const response = await fetch(`${API_BASE}/${id}`, {
    method: "PATCH",
    body: formData,
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("Failed to update bird");
  }

  return response.json();
}

/* =========================
   DELETE
========================= */

export async function deleteBird(id: string): Promise<void> {
  const response = await fetch(`${API_BASE}/${id}`, {
    method: "DELETE",
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("Failed to delete bird");
  }
}
