//fetch function - src/api/sightings.ts

import {Post} from "../types/Post";
const BASE_URL = "http://localhost:8080";

export async function getSightingById(postId:string): Promise<Post>{
  const response = await fetch(`${BASE_URL}/sightings/${postId}`,{credentials: 'include'});

  if (!response.ok){
    if (response.status == 404){
      throw new Error("Post not found");
    }
    throw new Error("Failed to fetch Post");
  }// if response not ok

  return response.json();
}//get sighting by Id

export async function getSightings(): Promise<Post[]>{
  const response = await fetch(`${BASE_URL}/sightings`,{credentials: 'include'});

    if (!response.ok){
    if (response.status == 404){
      throw new Error("Posts not found");
    }
    throw new Error("Failed to fetch Post");
  }// if response not ok

  return response.json();
}

export async function getSightingsByGroup(groupId: string): Promise<Post[]> {
  console.log('Calling API:', `${BASE_URL}/sightings/group/${groupId}`);
  console.log('Document cookies:', document.cookie);
  
  const response = await fetch(`${BASE_URL}/sightings/group/${groupId}`, {
    method: 'GET',
    credentials: 'include',
    mode: 'cors',
    headers: {
      'Content-Type': 'application/json',
    }
  });

  console.log('Response status:', response.status);
  console.log('Response headers:', response.headers);

  if (!response.ok) {
    if (response.status === 404) {
      return [];
    }
    const errorText = await response.text();
    console.error('Error response:', errorText);
    throw new Error(`Failed to fetch group posts: ${response.status} - ${errorText}`);
  }

  return response.json();
}

// Like a post
export async function likePost(postId: string, userId: string): Promise<Post> {
  const response = await fetch(`${BASE_URL}/sightings/${postId}/like/${userId}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    }
  });

  if (!response.ok) {
    throw new Error("Failed to like post");
  }

  return response.json();
}

// Unlike a post
export async function unlikePost(postId: string, userId: string): Promise<Post> {
  const response = await fetch(`${BASE_URL}/sightings/${postId}/unlike/${userId}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    }
  });

  if (!response.ok) {
    throw new Error("Failed to unlike post");
  }

  return response.json();
}

// Add a comment
export async function addComment(postId: string, userId: string, commentText: string): Promise<Post> {
  const response = await fetch(`${BASE_URL}/sightings/${postId}/comments?userId=${userId}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      textBody: commentText
    })
  });

  if (!response.ok) {
    throw new Error("Failed to add comment");
  }

  return response.json();
}

// Update a comment
export async function updateComment(postId: string, updatedComment: Comment): Promise<Post> {
  const response = await fetch(`${BASE_URL}/sightings/${postId}/comments`, {
    method: 'PATCH',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(updatedComment)
  });

  if (!response.ok) {
    throw new Error("Failed to update comment");
  }

  return response.json();
}

// Delete a comment
export async function deleteComment(postId: string, comment: Comment): Promise<Post> {
  const response = await fetch(`${BASE_URL}/sightings/${postId}/comments`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(comment)
  });

  if (!response.ok) {
    throw new Error("Failed to delete comment");
  }

  return response.json();
}

// Delete a post
export async function deletePost(postId: string, userId: string): Promise<void> {
  const response = await fetch(`${BASE_URL}/sightings/${postId}?userId=${userId}`, {
    method: 'DELETE',
    credentials: 'include',
  });

  if (!response.ok) {
    throw new Error("Failed to delete post");
  }
}

// Update a post
export async function updatePost(postId: string, userId: string, postData: any, image?: File | null): Promise<Post> {
  const formData = new FormData();
  formData.append('post', JSON.stringify(postData));
  formData.append('userId', userId);
  if (image) {
    formData.append('image', image);
  }

  const response = await fetch(`${BASE_URL}/sightings/${postId}`, {
    method: 'PUT',
    credentials: 'include',
    body: formData
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Failed to update post');
  }

  return response.json();
}