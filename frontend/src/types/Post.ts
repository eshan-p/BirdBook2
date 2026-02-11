import { Comment } from "../types/Comment";

export interface Coordinates {
  latitude: number;
  longitude: number;
}

export interface Post {
  id: string;
  header: string;
  
  tags?: {
    latitude?: string;
    longitude?: string;
  };

  bird: string | null;
  flagged: boolean;

  group?: string | null;
  help: boolean;

  likes: string[];

  image?: string | null;
  textBody: string;
  
  timestamp: string;
  comments: Comment[];

  user: {
    profilePic: string;
    userId: string;
    username: string;
  };
}