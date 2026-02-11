import { User } from "../types/User";
const BASE_URL = "http://localhost:8080";

export async function getAllUsers(): Promise<User[]> {
  const response = await fetch(`${BASE_URL}/users`, {credentials: 'include'});

  if (!response.ok) {
    throw new Error("Failed to fetch users");
  }

  return response.json();
}

export async function getUserById(userId: string): Promise<User> {
  const response = await fetch(`${BASE_URL}/users/${userId}`, {credentials: 'include'});

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error("User not found");
    }
    throw new Error("Failed to fetch User");
  }

  return response.json();
}

export async function getFriends(userId: string): Promise<User[]> {
  const response = await fetch(`${BASE_URL}/users/${userId}/friends `, {credentials: 'include'});

  if (!response.ok) {
    throw new Error("Failed to fetch friends");
  }

  return response.json();
}

export async function addFriend(userId: string, friendId: string): Promise<void> {
  const response = await fetch(
    `${BASE_URL}/users/${userId}/friends/${friendId}`,
    {
      method: "PUT",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error("Failed to add friend");
  }

  //return response.json();
}

export async function removeFriend(userId: string, friendId: string): Promise<void> {
  const response = await fetch(
    `${BASE_URL}/users/${userId}/friends/${friendId}`,
    {
      method: "DELETE",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error("Failed to remove friend");
  }

  //return response.json();
}

export async function updateUserRole(userId: string, role: string): Promise<User> {
  const response = await fetch(
    `${BASE_URL}/users/${userId}/role`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify({ role }),
    }
  );

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Failed to update user role: ${response.status}`);
  }

  return response.json();
}