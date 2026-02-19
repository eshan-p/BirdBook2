import React, { useEffect, useState } from 'react';
import ProfileIcon from '../components/common/ProfileIcon';
import TopBirds from '../components/features/TopBirds';
import MapView from '../components/features/MapView';
import { Bird } from '../types/Bird';
import { Post } from '../types/Post';
import { useAuth } from '../context/AuthContext';
import { useNavigate, useParams } from 'react-router-dom';
import { User } from '../types/User';
import { getUserById } from '../api/Users';
import { arrayToCoords, reverseCoordsToCityState } from '../utils/geolocation';
import { resolveMediaUrl } from '../utils/mediaUrl';
import { addFriend,removeFriend } from '../api/Users';

function OtherProfile() {
  const [posts, setPosts] = useState<Post[]>([]);
  const {userId} = useParams<{userId:string}>();
  const [pageLoading, setPageLoading] = useState<boolean>(true);
  const [locationName, setLocationName] = useState<string>("");
  const [userInfo, setUserInfo] = useState<User | null>(null);
  const [topBirds, setTopBirds] = useState<any[]>([]); //TODO: make typing more specific
  const navigate = useNavigate();
  const BASE_URL = "http://localhost:8080";
  const { user } = useAuth();
  const [currentUserFull, setCurrentUserFull] = useState<User | null>(null);
  const [currentUserLoading, setCurrentUserLoading] = useState(true);
  const [isFollowing, setIsFollowing] = useState(false);

  useEffect(() => {
    if (user?.id) {
      setCurrentUserLoading(true);
      getUserById(user.id)
        .then(setCurrentUserFull)
        .finally(() => setCurrentUserLoading(false));
    }
  }, [user?.id]);


  useEffect(() => {
    if(userId){
      fetch(`${BASE_URL}/users/${userId}/top-birds`, {credentials: 'include'})
        .then(r => r.json())
        .then(setTopBirds)
        .catch(err => console.error("Failed to fetch birds: ", err))
    }
  }, [userId])

  const topBirdsMapped: Bird[] = topBirds.map((b, i) => ({
    id: String(i),
    commonName: b.commonName,
    scientificName: b.scientificName,
    imageURL: b.imageURL,
    location: b.location ? [b.location[0], b.location[1]] : null
  }))

  useEffect(() => {
    if(userId){
        fetch(`${BASE_URL}/users/${userId}/posts`, {credentials: 'include'})
            .then(r => r.json())
            .then(setPosts)
            .catch(err => console.error("Failed to fetch posts:", err))
            .finally(() => setPageLoading(false));
    }
  }, [userId]);

  useEffect(() => {
    if(!userId && !pageLoading){
      navigate('/login')
      console.error("Not signed in!")
    }
  }, [userId, pageLoading, navigate]);

  useEffect(() => {
    if(userId) {
        getUserById(userId)
            .then(setUserInfo)
            .catch(err => console.error("Failed to fetch user: " + err));
    }
  }, [userId])

  useEffect(() => {
    if(userInfo?.location){
        reverseCoordsToCityState(arrayToCoords([Number(userInfo.location.latitude),Number(userInfo.location.longitude)])).then(setLocationName)
    }
  }, [userInfo?.location])

  useEffect(() => {
    if (currentUserFull && userInfo) {
      const isFriend = currentUserFull.friends?.some(
        friendId => String(friendId) === String(userInfo.id)
      );
      setIsFollowing(isFriend || false);
    }
  }, [currentUserFull, userInfo]);

  if (pageLoading || currentUserLoading) {
    return <div>loading page</div>;
  }

  if (!userInfo || !currentUserFull) {
    return <div>loading user data</div>;
  }

console.log(userInfo);
console.log(user);
console.log("currentUserFull.friends:", currentUserFull.friends);
console.log("viewed userId:", userId);
console.log("Type of userId: ",typeof(userId));
if (currentUserFull.friends){
    console.log("Type of currentUserFull.friends[0]: ",typeof(currentUserFull.friends[0]));
    console.log("Type of currentUserFull.friends[1]: ",typeof(currentUserFull.friends[1]));
    console.log("Type of currentUserFull.friends[2]: ",typeof(currentUserFull.friends[2]));
    console.log("Type of currentUserFull.friends[3]: ",typeof(currentUserFull.friends[3]));
    console.log("Type of currentUserFull.friends[4]: ",typeof(currentUserFull.friends[4]));
}

const canFollow =
  user &&
  userInfo &&
  user.id !== userInfo.id;

  return (
    <div className='flex flex-row h-full bg-[#F7F7F7] px-16'>
      <div className='basis-2/3 m-6'>
        <div className='bg-white h-fit w-full p-4 drop-shadow flex flex-col'>
            <div className='flex flex-row py-8 border-b border-gray-300 mb-3 px-3'>
                <ProfileIcon size="lg"/>
                <div>
                    <h2 className='text-xl mt-1 ml-4'>{userInfo.firstName} {userInfo.lastName}</h2>
                    <div className='flex flex-row ml-4 mb-2'>
                        <img src="/src/assets/pin.svg" alt="location"/>
                        <p className='text-base/4 opacity-65 ml-1'>{userInfo?.location || 'Location unkown'}</p>
                    </div>
                    <div className='flex flex-row items-center w-full justify-start px-3 gap-5'>
                        <div className='flex flex-col items-center'>
                            <p className='text-xl font-light text-[#0700D3]'>{userInfo.posts.length}</p>
                            <p className='text-sm font-extralight'>Spottings</p>
                        </div>
                        <div className='flex flex-col items-center'>
                            <p className='text-xl font-light text-[#0700D3]'>{userInfo.friends?.length || '0'}</p>
                            <p className='text-sm font-extralight'>Friends</p>
                        </div>
                        <div className='flex flex-col items-center'>
                            <p className='text-xl font-light text-[#0700D3]'>{userInfo.groups.length}</p>
                            <p className='text-sm font-extralight'>Groups</p>
                        </div>
                    </div>
                </div>
            </div>
            <div className='px-3 flex flex-col pb-6 border-b border-gray-300 mb-3'>
                <TopBirds birds={topBirdsMapped}/>
            </div>
            <div className='px-3 flex flex-col pb-6'>
                <h2 className='text-xl opacity-70 mb-4'>Sighting Map</h2>
                <MapView posts={posts}/>
            </div>
        </div>
      </div>
      <div className='basis-1/3 m-6 ml-0'>

{/* Profile Actions */}
{user && userId && userInfo && currentUserFull && user.id !== userInfo.id && (
  <div className="bg-white p-4 drop-shadow mb-6">
    <h3 className="text-lg font-medium mb-3"></h3>

    {(!isFollowing) ? (
      <button
  onClick={async () => {
    if (!user || !userId) return;

    try {
      await addFriend(user.id, userId);
      setCurrentUserFull(prev =>
        prev
          ? {
              ...prev,
              friends: [...new Set([...(prev.friends ?? []).map(String), String(userId)])],
            }
          : prev
      );
      setIsFollowing(true);
    } catch (err) {
      console.error("Failed to follow:", err);
    }
  }}
  className="w-full py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
>
  Friend
</button>
    ) : (
      <button
  onClick={async () => {
    if (!user || !userId) return;

    try {
      await removeFriend(user.id, userId);

      setCurrentUserFull(prev =>
  prev
    ? {
        ...prev,
        friends: (prev.friends ?? []).filter(
          id => String(id) !== String(userId)
        ),
      }
    : prev
);
setIsFollowing(false);
    } catch (err) {
      console.error("Failed to unfollow:", err);
    }
  }}
  className="w-full py-2 border border-gray-400 rounded hover:bg-gray-100 transition"
>
  Unfriend
</button>

    )}
  </div>
)}



        <div className='bg-white h-fit w-full p-4 drop-shadow mb-6'>
            <div className='flex flex-row w-full border-b border-gray-300 pb-2'>
                <img src="/src/assets/post.svg" alt="posts"/>
                <h3 className='ml-3 text-lg'>Posts</h3>
            </div>
          {posts.length === 0 ? (
            <p className='text-gray-500 text-sm text-center py-8'>No posts yet</p>
          ) : (
            <div className='grid grid-cols-4 gap-2'>
              {posts.map((post) => (
                <button
                  key={post.id}
                  onClick={() => navigate(`/sightings/${post.id}`)}
                  className='aspect-square bg-gray-100 rounded-lg overflow-hidden hover:opacity-80 transition-opacity relative group'
                >
                  {post.image ? (
                    <img 
                      src={resolveMediaUrl(post.image)}
                      alt={post.header}
                      className='w-full h-full object-cover'
                    />
                  ) : (
                    <div className='w-full h-full flex items-center justify-center bg-gradient-to-br from-blue-100 to-blue-200'>
                      <div className='text-center'>
                        {post.bird && typeof post.bird === 'object' && 'commonName' in post.bird ? (
                          <p className='text-sm font-medium text-gray-700'>
                            {(post.bird as any).commonName}
                          </p>
                        ) : (
                          <p className='text-xs text-gray-600'>{post.header.substring(0, 20)}...</p>
                        )}
                      </div>
                    </div>
                  )}
                  
                  {/* Overlay on hover */}
                  <div className='absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-4'>
                    <div className='flex flex-col items-center gap-1'>
                      <svg className='w-5 h-5 text-white' fill='currentColor' viewBox='0 0 24 24'>
                        <path d='M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z' />
                      </svg>
                      <span className='text-white text-xs font-medium'>{post.likes.length}</span>
                    </div>
                    <div className='flex flex-col items-center gap-1'>
                      <svg className='w-5 h-5 text-white' fill='none' stroke='currentColor' viewBox='0 0 24 24'>
                        <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2} d='M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z' />
                      </svg>
                      <span className='text-white text-xs font-medium'>{post.comments.length}</span>
                    </div>
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default OtherProfile;
