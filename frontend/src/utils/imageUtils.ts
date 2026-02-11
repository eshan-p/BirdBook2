export function getBirdImageUrl(imageURL: string | null | undefined): string {
  // Default placeholder if no image URL
  if (!imageURL) {
    return "/default-bird.jpg";
  }

  if (imageURL.startsWith("http://") || imageURL.startsWith("https://")) {
    return imageURL;
  }

  const BASE_URL = "http://localhost:8080";
  
  const cleanPath = imageURL.startsWith("/") ? imageURL.substring(1) : imageURL;
  
  return `${BASE_URL}/${cleanPath}`;
}
