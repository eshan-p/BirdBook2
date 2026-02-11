import React, { useEffect, useState, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import PostCard from '../components/features/PostCard'
import ProfileCard from '../components/features/ProfileCard'
import ProfileIcon from '../components/common/ProfileIcon'
import { Post } from '../types/Post'
import { Group, PostUser } from '../types/Group'
import { parseDate } from '../utils/dateTime'
import { getSightings, getSightingsByGroup } from '../api/Sightings'
import { 
  getAllGroups, 
  getUserGroups, 
  requestToJoinGroup, 
  leaveGroup, 
  approveJoinRequest, 
  denyJoinRequest, 
  removeMember, 
  updateGroup, 
  deleteGroup,
  getJoinRequests
} from '../api/Groups'
import { useAuth } from '../context/AuthContext'
import { getUserById } from '../api/Users'
import { User } from '../types/User'
import SearchBar from '../components/common/SearchBar'
import BirdCard from '../components/features/BirdCard'
import { Bird } from '../types/Bird'
import GroupFormCard from '../components/common/GroupFormCard'
import CreatePost from '../components/features/CreatePost'
import { getAllBirds } from '../api/Birds'
import FriendCard from '../components/features/FriendCard'
import { Friend } from '../types/Friend'
import GroupCard from '../components/features/GroupCard'
import { isAdmin } from '../utils/roleUtils'
import { arrayToCoords, reverseCoordsToCityState } from '../utils/geolocation'

const PAGE_SIZE = 5;

function GroupFeed() {
  const { groupId } = useParams<{ groupId: string }>();
  const { user, loading } = useAuth();
  const [userData, setUserData] = useState<User | null>(null);
  const [group, setGroup] = useState<Group | null>(null);
  const [posts, setPosts] = useState<Post[]>([]);
  const [loadingPage, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [birds, setBirds] = useState<Bird[]>([]);
  const [friends, setFriends] = useState<Friend[]>([]);
  const [groups, setGroups] = useState<Group[]>([]);
  const [joinRequests, setJoinRequests] = useState<PostUser[]>([]);
  const [isEditing, setIsEditing] = useState(false);
  const [isMember, setIsMember] = useState(false);
  const [isOwner, setIsOwner] = useState(false);
  const [hasRequested, setHasRequested] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const isSubmittingRef = useRef(false);
  const BASE_URL = "http://localhost:8080";
  const navigate = useNavigate();

  // Fetch full user data
  useEffect(() => {
    if (user?.id) {
      getUserById(user.id)
        .then(setUserData)
        .catch(console.error);
      
      fetch(`${BASE_URL}/users/${user.id}/friends`, {credentials: 'include'})
        .then(r => r.json())
        .then(setFriends)
        .catch(err => console.error("Failed to fetch user: " + err));
    }
  }, [user?.id]);

  useEffect(() => {
    if(user?.id) {
        getUserGroups(user.id)
            .then(setGroups)
            .catch(err => console.error("Failed to fetch groups:", err));
    }
  }, [user?.id]);

  useEffect(() => {
    getAllBirds()
      .then(setBirds)
      .catch(err => console.error("Failed to fetch birds:", err));
  }, []);

  // Check membership status
  useEffect(() => {
    if (!group || !user?.id) return;
    
    setIsOwner(group.owner.userId === user.id);
    setIsMember(group.members?.some(m => m.userId === user.id) || false);
    
    // Check if user is in requests array
    // Handle MongoDB ObjectId format - sometimes comes as object with timestamp/date
    const userInRequests = group.requests?.some(r => {
      let requestUserId: string;
      
      // If userId is an object (MongoDB ObjectId), we need to extract the actual ID
      if (typeof r.userId === 'object' && r.userId !== null) {
        // Try common ObjectId formats
        requestUserId = (r.userId as any).$oid || (r.userId as any).toString?.() || String(r.userId);
      } else {
        requestUserId = String(r.userId);
      }
      
      return requestUserId === user.id;
    }) || false;
    
    setHasRequested(userInRequests);
  }, [group, user?.id]);

  // Fetch join requests (for group owner only)
  useEffect(() => {
    if (!groupId || !user?.id || !isOwner) return;
    
    getJoinRequests(groupId)
      .then(setJoinRequests)
      .catch(console.error);
  }, [groupId, user?.id, isOwner]);

  // Fetch group details
  useEffect(() => {
    if (!groupId) return;
    
    getAllGroups()
      .then(groups => {
        const foundGroup = groups.find(g => g.id === groupId);
        setGroup(foundGroup || null);
      })
      .catch(err => console.error("Failed to fetch group:", err));
  }, [groupId]);

  // Fetch posts for this group
  useEffect(() => {
    if (!groupId) return;

    console.log('Fetching posts for group:', groupId);
    
    getSightingsByGroup(groupId)
      .then(posts => {
        console.log('Fetched posts:', posts);
        setPosts(posts);
      })
      .catch(err => {
        console.error('Error fetching posts:', err);
        setError(err.message);
      })
      .finally(() => setLoading(false));
  }, [groupId]);

  // Reset page when posts change
  useEffect(() => {
    setPage(0);
  }, [posts]);

  // Redirect if not logged in
  useEffect(() => {
    if (!user && !loading) {
      navigate("/login");
    }
  }, [user, loading, navigate]);

  // Handler functions
  const handleJoinRequest = async () => {
    // Use ref for immediate blocking (synchronous check)
    if (!groupId || !user?.id || isSubmittingRef.current) return;
    
    // Double-check database state before proceeding
    if (hasRequested) return;
    
    isSubmittingRef.current = true;
    setIsSubmitting(true);
    try {
      await requestToJoinGroup(groupId, user.id);
      
      // IMMEDIATELY set hasRequested to true
      setHasRequested(true);
      
      // Also manually update the group state to include the user in requests
      // Create a PostUser object with the correct structure
      if (group && userData) {
        const postUser: PostUser = {
          userId: userData.id,
          username: userData.username,
          profilePic: userData.profilePic
        };
        const updatedGroup = {
          ...group,
          requests: [...(group.requests || []), postUser]
        };
        setGroup(updatedGroup);
      }
    } catch (err: any) {
      console.error(err);
      const errorMessage = err.message || 'Failed to send join request';
      if (errorMessage.includes('already')) {
        // Refresh group data to sync with database
        const groups = await getAllGroups();
        const foundGroup = groups.find(g => g.id === groupId);
        setGroup(foundGroup || null);
      } else {
        alert(errorMessage);
      }
    } finally {
      isSubmittingRef.current = false;
      setIsSubmitting(false);
    }
  };

  const handlePostCreated = () => {
    getSightings()
    .then(data => {
      const sorted = data.sort((a, b) => {
        return new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime();
      });
      setPosts(sorted)
    })
    .catch(err => setError(err.message))
    .finally(() => {
      setLoading(false);
      console.log(posts);
    });
  }

  const handleLeaveGroup = async () => {
    if (!groupId || !user?.id) return;
    if (!confirm('Are you sure you want to leave this group?')) return;
    
    try {
      await leaveGroup(groupId, user.id);
      setIsMember(false);
      navigate('/groups');
    } catch (err) {
      console.error(err);
      alert('Failed to leave group');
    }
  };

  const handleApprove = async (userId: string) => {
    if (!groupId) return;
    try {
      await approveJoinRequest(groupId, userId);
      setJoinRequests(prev => prev.filter(r => r.userId !== userId));
      // Refresh group data
      const groups = await getAllGroups();
      const foundGroup = groups.find(g => g.id === groupId);
      setGroup(foundGroup || null);
    } catch (err) {
      console.error(err);
      alert('Failed to approve request');
    }
  };

  const handleDeny = async (userId: string) => {
    if (!groupId) return;
    try {
      await denyJoinRequest(groupId, userId);
      setJoinRequests(prev => prev.filter(r => r.userId !== userId));
    } catch (err) {
      console.error(err);
      alert('Failed to deny request');
    }
  };

  const handleRemoveMember = async (userId: string) => {
    if (!groupId) return;
    if (!confirm('Are you sure you want to remove this member?')) return;
    
    try {
      await removeMember(groupId, userId);
      // Refresh group data
      const groups = await getAllGroups();
      const foundGroup = groups.find(g => g.id === groupId);
      setGroup(foundGroup || null);
    } catch (err) {
      console.error(err);
      alert('Failed to remove member');
    }
  };

  const handleUpdateGroup = (updatedGroup: Group) => {
    setGroup(updatedGroup);
    setIsEditing(false);
  };

  const handleDeleteGroup = async () => {
    if (!groupId || !user?.id) return;
    if (!confirm('Are you sure you want to delete this group? This action cannot be undone.')) return;
    
    try {
      await deleteGroup(groupId, user.id);
      navigate('/groups');
    } catch (err: any) {
      console.error(err);
      alert(err.message || 'Failed to delete group');
    }
  };

  const totalPages = Math.ceil(posts.length / PAGE_SIZE);
  const pagedPosts = posts.slice(
    page * PAGE_SIZE,
    (page + 1) * PAGE_SIZE
  );

  if (loadingPage) return <p>Loading...</p>;

  return (
    <div className='flex flex-row h-full bg-[#F7F7F7] px-16'>
      {/* Left Sidebar */}
      <div className='flex flex-col basis-1/4 m-6 mr-0'>
        <ProfileCard user={userData || undefined}/>
        
        {/* Group Info */}
        {group && (
          <div className='h-fit w-full mt-6 bg-white p-4 drop-shadow'>
            <div className='flex flex-row w-full border-b border-gray-300 mb-3'>
              <p className='text-lg font-bold'>{group.name}</p>
            </div>
            <p className='text-sm text-gray-600 mb-2'>
              Owner: {group.owner.username}
            </p>
            {group.description && (
              <p className='text-sm text-gray-600 mb-2 italic'>
                {group.description}
              </p>
            )}
            <p className='text-sm text-gray-600 mb-2'>
              {group.members?.length || 0} members
            </p>
            {group.followers && (
              <p className='text-sm text-gray-600 mb-3'>
                {group.followers} followers
              </p>
            )}

            {/* Owner Controls */}
            {isOwner && (
              <div className='mt-3 border-t border-gray-300 pt-3 space-y-2'>
                <button 
                  onClick={() => setIsEditing(true)} 
                  className='w-full px-3 py-2 bg-blue-600 text-white text-sm rounded hover:bg-blue-700'
                >
                  Edit Group
                </button>
                <button 
                  onClick={handleDeleteGroup} 
                  className='w-full px-3 py-2 bg-red-600 text-white text-sm rounded hover:bg-red-700'
                >
                  Delete Group
                </button>
              </div>
            )}

            {/* Join/Leave Buttons (Non-owners) */}
            {!isOwner && (
              <div className='mt-3 border-t border-gray-300 pt-3'>
                {!isMember && !hasRequested && (
                  <button 
                    onClick={handleJoinRequest}
                    disabled={isSubmitting}
                    className='w-full px-3 py-2 bg-blue-600 text-white text-sm rounded hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed'
                  >
                    {isSubmitting ? 'Sending...' : 'Request to Join'}
                  </button>
                )}
                {!isMember && hasRequested && (
                  <div className='px-3 py-2 bg-yellow-50 border border-yellow-300 text-yellow-800 text-sm rounded text-center'>
                    Request pending...
                  </div>
                )}
                {isMember && (
                  <button 
                    onClick={handleLeaveGroup} 
                    className='w-full px-3 py-2 bg-red-600 text-white text-sm rounded hover:bg-red-700'
                  >
                    Leave Group
                  </button>
                )}
              </div>
            )}

            {/* Join Requests Section (Group owner only) */}
            {isOwner && joinRequests.length > 0 && (
              <div className='mt-3 border-t border-gray-300 pt-3'>
                <p className='text-sm font-semibold mb-2'>Join Requests:</p>
                <ul className='space-y-2'>
                  {joinRequests.map((request) => (
                    <li key={request.userId} className='flex items-center justify-between p-2 bg-gray-50 rounded'>
                      <div className='flex items-center gap-2'>
                        <ProfileIcon size="sm" src={request.profilePic ? `http://localhost:8080${request.profilePic}` : undefined} />
                        <span className='text-sm truncate'>{request.username}</span>
                      </div>
                      <div className='flex gap-1'>
                        <button 
                          onClick={() => handleApprove(request.userId)} 
                          className='px-3 py-1 bg-green-600 text-white text-xs rounded hover:bg-green-700'
                          title='Approve'
                        >
                          ✓
                        </button>
                        <button 
                          onClick={() => handleDeny(request.userId)} 
                          className='px-3 py-1 bg-red-600 text-white text-xs rounded hover:bg-red-700'
                          title='Deny'
                        >
                          ✗
                        </button>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            )}
            
            {/* Members List */}
            {group.members && group.members.length > 0 && (
              <div className='mt-3 border-t border-gray-300 pt-3'>
                <p className='text-sm font-semibold mb-2'>Members:</p>
                <ul className='text-sm text-gray-700 space-y-2'>
                  {group.members.map((member, index) => (
                    <li 
                      key={member.userId || `member-${index}`} 
                      className='flex items-center justify-between hover:bg-gray-50 p-1 rounded'
                    >
                      <div 
                        className='flex items-center gap-2 cursor-pointer flex-1'
                        onClick={() => member.userId && navigate(`/profile/${member.userId}`)}
                      >
                        <ProfileIcon size="sm" src={member.profilePic ? `http://localhost:8080${member.profilePic}` : undefined} />
                        <span className='truncate'>{member.username || 'Unknown User'}</span>
                      </div>
                      {isOwner && member.userId !== group.owner.userId && (
                        <button 
                          onClick={(e) => {
                            e.stopPropagation();
                            handleRemoveMember(member.userId);
                          }} 
                          className='px-2 py-1 bg-red-600 text-white text-xs rounded hover:bg-red-700'
                          title='Remove member'
                        >
                          Remove
                        </button>
                      )}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Main Feed */}
      <div className='basis-1/2 m-6'>
        <div className='flex flex-col'>
          {isMember && <CreatePost onPostCreated={handlePostCreated}/>}
          {error && (
            <div className="mb-4 p-4 bg-red-100 text-red-700 rounded">
              Error: {error}
            </div>
          )}
          
          {posts.length === 0 ? (
            <div className='bg-white p-6 text-center text-gray-500'>
              No posts yet in this group
            </div>
          ) : (
            pagedPosts.map(post => (
              <button
                key={post.id?.toString()}
                onClick={() => navigate(`/sightings/${post.id.toString()}`)}
                className="mb-6"
              >
                <PostCard
                  description={post.header}
                  author={post.user.username}
                  authorId={post.user.userId}
                  dateTime={parseDate(post.timestamp)}
                  location={post.tags?.latitude && post.tags?.longitude 
                    ? {
                        latitude: parseFloat(post.tags.latitude),
                        longitude: parseFloat(post.tags.longitude)
                      }
                    : undefined
                  }
                  likes={post.likes.length}
                  comments={post.comments.length}
                  image={post.image}
                />
              </button>
            ))
          )}
        </div>
      </div>

      {/* Right Sidebar */}
      <div className='basis-1/4 m-6 ml-0 h-fit w-full bg-white p-4 drop-shadow'>
        <div className='flex flex-row w-full border-b border-gray-300 mb-3 items-center'>
          <img src="src/assets/bird.svg" alt="birds" className='w-5 h-5'/>
          <div className='text-lg ml-3 font-bold'>All Birds</div>
        </div>
        {birds.length === 0 ? (
          <p className='text-sm'>Loading...</p>
        ) : (
          birds.slice(0, 20).map(bird => (
            <div key={bird.id} className='flex items-center gap-2 mb-2'>
              {bird.imageURL && (
                <img 
                  src={bird.imageURL} 
                  alt={bird.commonName}
                  className='w-8 h-8 rounded-full object-cover'
                />
              )}
              <p className='text-sm'>
                {bird.commonName}
              </p>
            </div>
          ))
        )}
      </div>

      {/* Edit Group Modal */}
      {isEditing && group && (
        <GroupFormCard 
          onClose={() => setIsEditing(false)} 
          group={group}
          onUpdate={handleUpdateGroup}
        />
      )}
    </div>
  )
}

export default GroupFeed;