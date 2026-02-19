function readApiBaseUrl(): string {
  const fromWindow =
    typeof window !== "undefined" &&
    typeof (window as { __API_BASE_URL__?: string }).__API_BASE_URL__ === "string"
      ? (window as { __API_BASE_URL__?: string }).__API_BASE_URL__
      : "";

  const fromProcess =
    typeof process !== "undefined" &&
    process.env &&
    typeof process.env.VITE_API_BASE_URL === "string"
      ? process.env.VITE_API_BASE_URL
      : "";

  return (fromWindow || fromProcess || "").replace(/\/$/, "");
}

const API_BASE_URL = readApiBaseUrl();

export function resolveMediaUrl(value?: string | null): string {
  if (!value) {
    return "";
  }

  if (value.startsWith("http://") || value.startsWith("https://")) {
    return value;
  }

  const normalized = value.startsWith("/") ? value : `/${value}`;
  return API_BASE_URL ? `${API_BASE_URL}${normalized}` : normalized;
}

export function resolveApiUrl(path: string): string {
  const normalized = path.startsWith("/") ? path : `/${path}`;
  return API_BASE_URL ? `${API_BASE_URL}${normalized}` : normalized;
}

export function defaultProfilePicUrl(): string {
  return resolveMediaUrl("/profile_pictures/default_pfp.jpg");
}
