import { Bird } from '../types/Bird';
import { User } from '../types/User';
import { Group } from '../types/Group';
import { Post } from '../types/Post';

const BASE_URL = "http://localhost:8080";

export interface SearchResults {
  birds: Bird[];
  users: User[];
  groups: Group[];
  posts: Post[];
}

export async function searchAll(query: string): Promise<SearchResults> {
  const response = await fetch(
    `${BASE_URL}/search?query=${encodeURIComponent(query)}`,
    { credentials: 'include' }
  );

  if (!response.ok) {
    throw new Error("Failed to search");
  }

  return response.json();
}

export async function searchBirds(query: string): Promise<Bird[]> {
  const response = await fetch(
    `${BASE_URL}/search/birds?query=${encodeURIComponent(query)}`,
    { credentials: 'include' }
  );

  if (!response.ok) {
    throw new Error("Failed to search birds");
  }

  return response.json();
}

export async function searchUsers(query: string): Promise<User[]> {
  const response = await fetch(
    `${BASE_URL}/search/users?query=${encodeURIComponent(query)}`,
    { credentials: 'include' }
  );

  if (!response.ok) {
    throw new Error("Failed to search users");
  }

  return response.json();
}

export async function searchFriends(query: string): Promise<User[]> {
  const response = await fetch(
    `${BASE_URL}/search/friends?query=${encodeURIComponent(query)}`,
    { credentials: 'include' }
  );

  if (!response.ok) {
    throw new Error("Failed to search friends");
  }

  return response.json();
}

export async function searchGroups(query: string): Promise<Group[]> {
  const response = await fetch(
    `${BASE_URL}/search/groups?query=${encodeURIComponent(query)}`,
    { credentials: 'include' }
  );

  if (!response.ok) {
    throw new Error("Failed to search groups");
  }

  return response.json();
}

export async function searchMyGroups(query: string): Promise<Group[]> {
  const response = await fetch(
    `${BASE_URL}/search/my-groups?query=${encodeURIComponent(query)}`,
    { credentials: 'include' }
  );

  if (!response.ok) {
    throw new Error("Failed to search groups");
  }

  return response.json();
}

export async function searchPosts(query: string): Promise<Post[]> {
  const response = await fetch(
    `${BASE_URL}/search/posts?query=${encodeURIComponent(query)}`,
    { credentials: 'include' }
  );

  if (!response.ok) {
    throw new Error("Failed to search posts");
  }

  return response.json();
}