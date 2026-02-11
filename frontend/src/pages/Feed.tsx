import React, { useEffect, useState } from 'react'
import PostCard from '../components/features/PostCard'
import ProfileIcon from '../components/common/ProfileIcon';
import ProfileCard from '../components/features/ProfileCard';
import { reverseCoordsToCityState } from '../utils/geolocation';
import GroupCard from '../components/features/GroupCard';
import { Group } from '../types/Group';
import FriendCard from '../components/features/FriendCard';
import { Friend } from '../types/Friend';
import { getSightings } from '../api/Sightings';
import { Post } from '../types/Post';
import { parseDate } from '../utils/dateTime';
import { Bird } from '../types/Bird';
import BirdCard from '../components/features/BirdCard';
import SearchBar from '../components/common/SearchBar';
import { getAllBirds } from '../api/Birds';
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from '../context/AuthContext';
import { getUserById } from '../api/Users';
import { User } from '../types/User';
import CreatePost from '../components/features/CreatePost';
import { getUserGroups } from '../api/Groups';
import { isBasicUser,isAdmin,isSuperUser } from '../utils/roleUtils';

//page logic
const PAGE_SIZE = 5; // easy to tweak later

function Feed() {
  const [groups, setGroups] = useState<Group[]>([]);
  const [posts, setPosts] = useState<Post[]>([]);
  const [loadingPage, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const BASE_URL = "http://localhost:8080";
  const { user, loading } = useAuth();
  const [userData, setUserData] = useState<User | null>(null);
  const [friends, setFriends] = useState<User[]>([]);
  const [birds, setBirds] = useState<Bird[]>([]);

  const [page, setPage] = useState(0); // zero-based index

  const [newlyLoadedPosts, setNewlyLoadedPosts] = useState<string[]>([]);

  const navigate = useNavigate();
  
  // Reset page if posts change
  useEffect(() => {
    setPage(0);
  }, [posts]);

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

useEffect(() => {
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
}, []); //get sightings

  useEffect(() => {
    if(user?.id) {
        getUserGroups(user.id)
            .then(setGroups)
            .catch(err => console.error("Failed to fetch groups:", err));
    }
  }, [user?.id]); // get user's groups only

  useEffect(() => {
    getAllBirds()
      .then(setBirds)
      .catch(err => console.error("Failed to fetch birds:", err));
  }, []); // get birds

  useEffect(() => {
      if(user?.id) {
          getUserById(user.id)
            .then(setUserData)
            .catch(console.error);
          fetch(`${BASE_URL}/users/${user.id}/friends`, {credentials: 'include'})
            .then(async (r) => {
      if (!r.ok) {
        throw new Error(`Failed to fetch friends (${r.status})`);
      }
      return r.json();
    })
    .then(setFriends)
    .catch(err => console.error("Failed to fetch friends:", err));
      }
    }, [user?.id])

useEffect(() => {
  const startIdx = page * PAGE_SIZE;
  const endIdx = Math.min(posts.length, (page + 1) * PAGE_SIZE);
  const newPosts = posts.slice(startIdx, endIdx).map(post => post.id.toString());
  setNewlyLoadedPosts(newPosts);
}, [page, posts]);

  const totalPages = Math.ceil(posts.length / PAGE_SIZE);

const pagedPosts = posts.slice(0, (page + 1) * PAGE_SIZE);

  if (loadingPage) return <p>Loading...</p>;

  //console.log("posts:", posts);
  //console.log("pagedPosts:", pagedPosts);
  console.log(friends);

  

  return (
    <div className='flex flex-row h-full bg-[#F7F7F7] px-16'>
        {/* Left Sidebar */}
        {user ? (
          <div className='flex flex-col basis-1/4 m-6 mr-0'>
            <ProfileCard user={userData || undefined}/>
            <div className='h-fit w-full mt-6 bg-white p-4 drop-shadow'>
              <div className='flex flex-row w-full border-b border-gray-300 mb-3'>
                <img src="src/assets/groups.svg" alt="groups"/>
                <p className='text-lg ml-3 font-bold'>Groups</p>
              </div>
              {groups.length === 0 ? (
                <p className='text-gray-500 text-sm text-center py-8'>No groups yet</p>
              ) : (
                <>
                  {groups.map((group) => (
                    <GroupCard key={group.id.toString()} group={group}/>
                  ))}
                </>
              )}
              <div className='flex flex-row w-full border-b border-gray-300 mb-3'>
                <img src="src/assets/person.svg" alt="friends"/>
                <p className='text-lg ml-3 font-bold'>Friends</p>
              </div>
              {friends.length === 0 ? (
                <p className='text-gray-500 text-sm text-center py-8'>No friends yet</p>
              ) : (
                <>
                  {friends.map((friend) => (
                    <FriendCard key={friend.id} user={friend}/>
                  ))}
                </>
              )}
            </div>
          </div>
        ): (
          <div className='flex flex-col basis-1/4 m-6 mr-0 h-fit w-full mt-6 bg-white p-4 drop-shadow'>
            <div className='text-lg text-center font-semibold'><Link to="/login" className='text-blue-500'>Login</Link> to view profile information, join groups, and add friends!</div>
          </div>
        )}

        {/* Main Feed */}
        <div className='basis-1/2 m-6 max-h-screen'>
          <div className='flex flex-col'>
            {user? (<CreatePost onPostCreated={handlePostCreated}/>):(<></>)}

            {pagedPosts.map(post => (
              <button
                key={post.id.toString()}
                onClick={() => navigate(`/sightings/${post.id.toString()}`)}
                className={`mb-6 transition-opacity duration-700 ${
                  newlyLoadedPosts.includes(post.id.toString()) ? 'animate-fadeIn' : ''
                }`}
              >
                <PostCard
                  description={post.header}
                  author={post.user.username}
                  authorId={post.user.userId}
                  authorProfilePic={post.user.profilePic}
                  dateTime={parseDate(post.timestamp)}
                  location={
                    post.tags?.latitude && post.tags?.longitude
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
            ))}

            {/* Load more button */}
            {page < totalPages - 1 && (
              <button
                onClick={() => setPage(prev => prev + 1)}
                className="w-full py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition mb-8"
              >
                Load more...
              </button>
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
    </div>
  )
}

export default Feed