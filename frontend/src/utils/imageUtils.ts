import { resolveMediaUrl } from './mediaUrl';

export function getBirdImageUrl(imageURL: string | null | undefined): string {
  // Default placeholder if no image URL
  if (!imageURL) {
    return "/default-bird.jpg";
  }

  return resolveMediaUrl(imageURL);
}
