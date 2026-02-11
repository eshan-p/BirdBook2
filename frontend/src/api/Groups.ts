import {Group} from "../types/Group";
import { PostUser } from "../types/Group";
const BASE_URL = "http://localhost:8080";

export async function getAllGroups(): Promise<Group[]>{
    try {
        const response = await fetch(`${BASE_URL}/groups`, {
            headers: { "Content-Type": "application/json" },
            credentials: "include"
        });
        
        if (!response.ok){
            if (response.status == 404){
                throw new Error("Groups not found");
            }
            throw new Error(`Failed to fetch Groups: ${response.status}`);
        }

        const data = await response.json();
        return data;
    } catch (error: any) {
        console.error("getAllGroups error:", error);
        throw error;
    }
}

export async function getUserGroups(userId:string): Promise<Group[]>{
    const response = await fetch(`${BASE_URL}/users/${userId}/groups`, {
        headers: { "Content-Type": "application/json" },
        credentials: "include"
    });

    if (!response.ok){
        if (response.status == 404){
            return [];
        }
        throw new Error("Failed to fetch user's groups");
    }

    return response.json();
}

export async function requestToJoinGroup(groupId: string, userId: string): Promise<void> {
  const response = await fetch(
    `${BASE_URL}/groups/${groupId}/join-requests?userId=${userId}`,
    { method: "POST", credentials: "include" }
  );

  if (!response.ok) {
    // Try to get the actual error message from backend
    const errorText = await response.text();
    throw new Error(errorText || "Failed to send join request");
  }
}

export async function leaveGroup(groupId: string, userId: string): Promise<void> {
  const response = await fetch(`${BASE_URL}/groups/${groupId}/members/${userId}`, {
    method: "DELETE",
    credentials: "include"
  });

  if (!response.ok) {
    throw new Error("Failed to leave group");
  }
}

export async function createGroup(name: string, ownerId: string): Promise<Group> {
  const response = await fetch(
    `${BASE_URL}/groups?name=${encodeURIComponent(name)}&ownerId=${ownerId}`,
    { method: "POST", credentials: "include" }
  );
  
  if (!response.ok) {
    throw new Error("Failed to create group");
  }
  const owner:PostUser = {
  userId : ownerId,
  username : ""
  };
  return response.text().then(() => ({ id: "", name, owner, members: [], requests: [] })); // placeholder until backend returns body
}

export async function deleteGroup(groupId: string, userId: string): Promise<void> {
  const response = await fetch(`${BASE_URL}/groups/${groupId}?userId=${userId}`, {
    method: "DELETE",
    credentials: "include"
  });

  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || "Failed to delete group");
  }
}

export async function approveJoinRequest(groupId: string, userId: string): Promise<void> {
  const response = await fetch(`${BASE_URL}/groups/${groupId}/join-requests/${userId}/approve`, {
    method: "PUT",
    credentials: "include"
  });

  if (!response.ok) {
    throw new Error("Failed to approve request");
  }
}

export async function denyJoinRequest(groupId: string, userId: string): Promise<void> {
  const response = await fetch(`${BASE_URL}/groups/${groupId}/join-requests/${userId}/deny`, {
    method: "PUT",
    credentials: "include"
  });

  if (!response.ok) {
    throw new Error("Failed to deny request");
  }
}

export async function updateGroup(groupId: string, name: string, userId: string): Promise<Group> {
  const response = await fetch(
    `${BASE_URL}/groups/${groupId}?userId=${userId}`,
    {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({ name })
    }
  );

  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || "Failed to update group");
  }

  return response.json();
}

export async function removeMember(groupId: string, userId: string): Promise<void> {
  const response = await fetch(
    `${BASE_URL}/groups/${groupId}/members/${userId}/remove`,
    {
      method: "DELETE",
      credentials: "include"
    }
  );

  if (!response.ok) {
    throw new Error("Failed to remove member");
  }
}

export async function getJoinRequests(groupId: string): Promise<PostUser[]> {
  const response = await fetch(
    `${BASE_URL}/groups/${groupId}/join-requests`,
    {
      credentials: "include"
    }
  );

  if (!response.ok) {
    throw new Error("Failed to fetch join requests");
  }

  return response.json();
}