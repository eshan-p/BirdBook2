import React from 'react';
import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

//Actual page
import { getSightingById, likePost, unlikePost, addComment, deletePost } from "../api/Sightings";
import { Post } from "../types/Post";
import { Comment } from "../types/Comment";
import { parseDate } from '../utils/dateTime';
import { getTimeSince } from '../utils/dateTime';
import { reverseCoordsToCityState } from '../utils/geolocation';
import ProfileCard from '../components/features/ProfileCard';
import ProfileIcon from '../components/common/ProfileIcon';
import { useAuth } from '../context/AuthContext';
import { getUserById } from '../api/Users';
import { User } from '../types/User';
import { isBasicUser,isAdmin,isSuperUser, canPerformAction, Role } from '../utils/roleUtils';
import PostFormCard from '../components/common/PostFormCard';
import { resolveMediaUrl } from '../utils/mediaUrl';

function Sighting() {
  //grabs params from the current url
  const {postId} = useParams<{postId:string}>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [userData, setUserData] = useState<User | null>(null);

  //these return value and functions to update the values
  const [post, setPost] = useState<Post | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [locationLabel, setLocationLabel] = useState<string | null>(null);
  const [timeSince, setTimeSince] = useState<string>('');
  const [newComment, setNewComment] = useState<string>('');
  const [isLiked, setIsLiked] = useState<boolean>(false);
  const [isSubmittingComment, setIsSubmittingComment] = useState(false);
  const [isTogglingLike, setIsTogglingLike] = useState(false);
  const [isEditing, setIsEditing] = useState(false);

  //first fetch post
  useEffect(() => {
    if (!postId) return;

    setLoading(true);
    
    getSightingById(postId)
      .then(setPost)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));

  }, [postId]);

  // Fetch full user data
  useEffect(() => {
    if (user?.id) {
      getUserById(user.id)
        .then(setUserData)
        .catch(console.error);
    }
  }, [user?.id]);

  // Check if current user has liked the post
  useEffect(() => {
    if (post && user) {
      setIsLiked(post.likes.some(like => like.toString() === user.id));
    }
  }, [post, user]);

  //finally fetch location
useEffect(() => {
  if (!post?.tags?.latitude || !post?.tags?.longitude) return;

  const latitude = parseFloat(post.tags.latitude);
  const longitude = parseFloat(post.tags.longitude);

  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
    console.error("Invalid coordinates:", { latitude, longitude });
    setLocationLabel(null);
    return;
  }

  reverseCoordsToCityState({ latitude, longitude })
    .then(setLocationLabel)
    .catch((err) => {
      console.error("Reverse geocode failed:", err);
      setLocationLabel(null);
    });
}, [post?.tags?.latitude, post?.tags?.longitude]);

  // Update time since periodically
  useEffect(() => {
    if (!post?.timestamp) return;
    
    const updateTime = () => {
      setTimeSince(getTimeSince(parseDate(post.timestamp)));
    };
    
    updateTime();
    const interval = setInterval(updateTime, 60000);
    return () => clearInterval(interval);
  }, [post?.timestamp]);

  const handleCommentSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim() || !user || !postId) return;

    setIsSubmittingComment(true);
    try {
      const updatedPost = await addComment(postId, user.id, newComment.trim());
      setPost(updatedPost);
      setNewComment('');
    } catch (err) {
      console.error("Failed to add comment:", err);
      alert("Failed to add comment. Please try again.");
    } finally {
      setIsSubmittingComment(false);
    }
  };

  const handleToggleLike = async () => {
    if (!user || !postId || isTogglingLike) return;

    setIsTogglingLike(true);
    try {
      const updatedPost = isLiked 
        ? await unlikePost(postId, user.id)
        : await likePost(postId, user.id);
      
      setPost(updatedPost);
      setIsLiked(!isLiked);
    } catch (err) {
      console.error("Failed to toggle like:", err);
      alert("Failed to update like. Please try again.");
    } finally {
      setIsTogglingLike(false);
    }
  };

  const handleDeletePost = async () => {
    if (!user?.id || !postId) return;
    if (!confirm('Are you sure you want to delete this post? This action cannot be undone.')) return;

    try {
      await deletePost(postId, user.id);
      navigate('/feed');
    } catch (err: any) {
      alert(err.message || 'Failed to delete post');
    }
  };

  const handleUpdatePost = async () => {
    if (!postId) return;
    setLoading(true);
    try {
      const updatedPost = await getSightingById(postId);
      setPost(updatedPost);
    } catch (err) {
      console.error("Failed to refresh post:", err);
    } finally {
      setLoading(false);
    }
  };

  // Helper to extract userId string from post
  const getPostOwnerId = (): string | undefined => {
    if (!post?.user) return undefined;
    const userId = post.user.userId;
    if (typeof userId === 'string') return userId;
    if (typeof userId === 'object' && userId !== null) {
      return (userId as any).$oid || (userId as any).toString?.() || String(userId);
    }
    return undefined;
  };

  if (loading) return <p className="text-center mt-8">Loading...</p>;
  if (error) return (
    <div className="flex justify-center mt-8">
      <div className="p-4 bg-red-100 text-red-700 rounded max-w-md">
        Error: {error}
      </div>
    </div>
  );
  if (!post) return <p className="text-center mt-8">Post not found</p>;

  return (
    <div className='flex flex-row h-full bg-[#F7F7F7] px-16'>
      {/* Left Sidebar */}
      {(isBasicUser(user?.role) || isAdmin(user?.role) || isSuperUser(user?.role))&&
      <div className='flex flex-col basis-1/4 m-6 mr-0'>
        <ProfileCard user={userData || undefined}/>
        
        {/* Post Info Card */}
        <div className='h-fit w-full mt-6 bg-white p-4 drop-shadow'>
          <div className='flex flex-row w-full border-b border-gray-300 mb-3 pb-2'>
            <p className='text-lg font-bold'>Post Info</p>
          </div>
          
          <div className='space-y-2 text-sm'>
            <div>
              <p className='text-gray-500'>Posted</p>
              <p className='font-medium'>{parseDate(post.timestamp).toDateString()}</p>
            </div>
          </div>

          {/* Owner/Admin Controls */}
          {user && canPerformAction(user.id, user.role, getPostOwnerId(), Role.ADMIN_USER) && (
            <div className='mt-3 border-t border-gray-300 pt-3 space-y-2'>
              <button 
                onClick={() => setIsEditing(true)} 
                className='w-full px-3 py-2 bg-blue-600 text-white text-sm rounded hover:bg-blue-700'
              >
                Edit Post
              </button>
              <button 
                onClick={handleDeletePost} 
                className='w-full px-3 py-2 bg-red-600 text-white text-sm rounded hover:bg-red-700'
              >
                Delete Post
              </button>
            </div>
          )}
        </div>
      </div>}

      {/* Main Content */}
      <div className='basis-3/4 m-6'>
        {/* Post Card */}
        <div className='w-full bg-white p-6 drop-shadow mb-6'>
          {/* User Info */}
          <div className='flex flex-row mb-4'>
            <ProfileIcon size="md" src={resolveMediaUrl(post.user?.profilePic) || undefined}/>
            <div className='h-14 w-full ml-3'>
              <h3 className='font-bold text-base'>{post.user ? post.user.username : "Unknown user"}</h3>
              {locationLabel && <p className='text-sm/3 opacity-85'>{locationLabel}</p>}
              <p className='text-sm/6 opacity-85'>{timeSince}</p>
            </div>
          </div>

          {/* Post Title */}
          <h1 className='text-2xl font-bold mb-4'>{post.header}</h1>

          {/* Post Body */}
          <p className='text-base leading-relaxed mb-4 whitespace-pre-wrap'>{post.textBody}</p>

          {/* Post Image */}
          {post.image && (
            <div className='mb-4'>
              <img 
                src={resolveMediaUrl(post.image)} 
                alt={post.header} 
                className='w-full rounded-lg object-cover max-h-96'
              />
            </div>
          )}

          {/* Engagement Stats - Interactive */}
          <div className='flex flex-row mt-4 pt-4 border-t border-gray-200'>
            <button 
              onClick={handleToggleLike}
              disabled={!user || isTogglingLike}
              className='flex flex-row items-center mr-6 hover:bg-gray-50 px-3 py-2 rounded transition disabled:opacity-50 disabled:cursor-not-allowed'
            >
              {!isLiked ? (
                <img 
                  src="/src/assets/heart.png" 
                  alt="likes" 
                  className='w-5 h-5 mr-2'
                />
              ) : (
                <img 
                  src="/src/assets/liked.svg" 
                  alt="likes" 
                  className='w-5 h-5 mr-2'
                />
              )}
              <p className='text-lg'>
                {post.likes.length} {post.likes.length === 1 ? 'like' : 'likes'}
              </p>
            </button>
            <div className='flex flex-row items-center px-3 py-2'>
              <img src="/src/assets/comment.png" alt="comments" className='w-5 h-5 mr-2'/>
              <p className='text-lg'>{post.comments.length} {post.comments.length === 1 ? 'comment' : 'comments'}</p>
            </div>
          </div>
        </div>

        {/* Comment Input Box */}
        {user ? (
          <div className='w-full bg-white p-6 drop-shadow mb-6'>
            <h3 className='text-lg font-bold mb-3'>Add a Comment</h3>
            <form onSubmit={handleCommentSubmit}>
              <div className='flex items-start gap-3'>
                <ProfileIcon size="sm" src={resolveMediaUrl(user?.profilePic) || undefined}/>
                <div className='flex-1'>
                  <textarea
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    placeholder="Share your thoughts..."
                    className='w-full p-3 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent'
                    rows={3}
                    disabled={isSubmittingComment}
                  />
                  <div className='flex justify-end mt-2'>
                    <button
                      type="submit"
                      disabled={!newComment.trim() || isSubmittingComment}
                      className='px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:bg-gray-300 disabled:cursor-not-allowed'
                    >
                      {isSubmittingComment ? 'Posting...' : 'Post Comment'}
                    </button>
                  </div>
                </div>
              </div>
            </form>
          </div>
        ) : (
          <div className='w-full bg-white p-6 drop-shadow mb-6 text-center text-gray-500'>
            Please log in to comment
          </div>
        )}

        {/* Comments Section */}
        <div className='w-full bg-white p-6 drop-shadow'>
          <h2 className='text-xl font-bold mb-4 pb-2 border-gray-300'>
            Comments ({post.comments.length})
          </h2>
          <CommentsList comments={post.comments} />
        </div>
      </div>

      {/* Edit Post Modal */}
      {isEditing && post && (
        <PostFormCard 
          onClose={() => setIsEditing(false)} 
          existingPost={post}
          onUpdate={handleUpdatePost}
        />
      )}
    </div>
  );
}

export default Sighting;

//keep this nested here in the sighting page, as its only used here
function CommentsList({comments}: {comments:Comment[]}){
  
  if(comments.length === 0){
    return (
      <div className='text-center py-8 text-gray-500'>
        <img src="/src/assets/comment.png" alt="no comments" className='w-12 h-12 mx-auto mb-2 opacity-50'/>
        <p>No comments yet. Be the first to comment!</p>
      </div>
    );
  }

  return (
    <div className='space-y-4'>
      {comments.map((comment) => (
        <CommentItem
          key={`${comment.user.id}-${comment.timestamp}`}
          comment={comment}
        />
      ))}
    </div>
  );
}

//comment item
function CommentItem({ comment }: { comment: Comment }) {
  const [timeSince, setTimeSince] = useState<string>('');

  useEffect(() => {
    const updateTime = () => {
      setTimeSince(getTimeSince(parseDate(comment.timestamp)));
    };
    
    updateTime();
    const interval = setInterval(updateTime, 60000);
    return () => clearInterval(interval);
  }, [comment.timestamp]);

  return (
    <div className='border-l-2 border-gray-200 pl-4 py-2'>
      <div className='flex items-start mb-2'>
        <ProfileIcon size="sm" src={resolveMediaUrl(comment.user?.profilePic) || undefined}/>
        <div className='ml-3 flex-1'>
          <div className='flex items-baseline gap-2'>
            <span className='font-semibold text-sm'>
              {comment.user ? comment.user.username : "Unknown user"}
            </span>
            <span className='text-xs text-gray-500'>·</span>
            <span className='text-xs text-gray-500'>{timeSince}</span>
          </div>
          <p className='text-sm text-gray-700 mt-1 leading-relaxed'>{comment.textBody}</p>
        </div>
      </div>
    </div>
  );
}