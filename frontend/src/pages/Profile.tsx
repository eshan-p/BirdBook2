import React, { useEffect, useState } from 'react';
import ProfileIcon from '../components/common/ProfileIcon';
import TopBirds from '../components/features/TopBirds';
import MapView from '../components/features/MapView';
import { Bird } from '../types/Bird';
import { Post } from '../types/Post';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { User } from '../types/User';
import { getUserById } from '../api/Users';
import { useSearchParams } from 'react-router-dom';
import EditProfileModal from '../components/common/EditProfileModal';
import { Badge } from '../types/Badge';
import { BADGES, getUnlockedBadges } from '../utils/badgeUtils';
import { BadgesDisplay } from '../components/common/BadgesDisplay';
import { Toast } from '../components/common/Toast';

function Profile() {
  const [posts, setPosts] = useState<Post[]>([]);
  const { user, loading } = useAuth();
  const [pageLoading, setPageLoading] = useState<boolean>(true);
  const [locationName, setLocationName] = useState<string>("");
  const [searchParams] = useSearchParams();
  const [isEditingProfile, setIsEditingProfile] = useState(
    searchParams.get('edit') === 'true'
  );
  const [userInfo, setUserInfo] = useState<User | null>(null);
  const [unlockedBadges, setUnlockedBadges] = useState<string[]>([]);
  const [toast, setToast] = useState<Badge | null>(null);
  const [topBirds, setTopBirds] = useState<any[]>([]);
  const navigate = useNavigate();
  const BASE_URL = "http://localhost:8080";

  useEffect(() => {
    if(user?.id){
      fetch(`${BASE_URL}/users/${user.id}/top-birds`, {credentials: 'include'})
        .then(r => r.json())
        .then(setTopBirds)
        .catch(err => console.error("Failed to fetch birds: ", err))
    }
  }, [user?.id])

  useEffect(() => {
    if (userInfo && posts) {
      const newUnlocked = getUnlockedBadges(userInfo, posts);
      const newBadges = newUnlocked.filter(b => !unlockedBadges.includes(b));
      if (newBadges.length > 0) {
        const badgeId = newBadges[0];
        const badge = BADGES[badgeId.toUpperCase().replace(/_/g, '_')];
        setToast(badge);
      }
      setUnlockedBadges(newUnlocked);
    }
  }, [userInfo, posts]);

  const topBirdsMapped: Bird[] = topBirds.map((b, i) => ({
    id: String(i),
    commonName: b.commonName,
    scientificName: b.scientificName,
    imageURL: b.imageURL,
    location: b.location ? [b.location[0], b.location[1]] : null
  }))

  useEffect(() => {
    if(user?.id){
        fetch(`${BASE_URL}/users/${user.id}/posts`, {credentials: 'include'})
            .then(r => r.json())
            .then(setPosts)
            .catch(err => console.error("Failed to fetch posts:", err))
            .finally(() => setPageLoading(false));
    }
  }, [user?.id]);

  useEffect(() => {
    if(!user && !loading){
      navigate('/login')
      console.error("Not signed in!")
    }
  }, [user, loading, navigate]);

  useEffect(() => {
    if(user?.id) {
        getUserById(user.id)
            .then(setUserInfo)
            .catch(err => console.error("Failed to fetch user: " + err));
    }
  }, [user?.id])

  if (loading) return <div>loading auth</div>
  if (!userInfo) return <div>loading user data</div>

  return (
    <div className='flex flex-row h-full bg-[#F7F7F7] px-16'>
      {isEditingProfile && (
        <EditProfileModal
          user={userInfo}
          onClose={() => setIsEditingProfile(false)}
          onSave={(updatedUser) => setUserInfo(updatedUser)}/>
      )}
      <div className='basis-2/3 m-6'>
        <div className='bg-white h-fit w-full p-4 drop-shadow flex flex-col'>
            <div className='flex flex-row py-8 border-b border-gray-300 mb-3 px-3'>
                <ProfileIcon size="lg" src={userInfo?.profilePic ? `http://localhost:8080${userInfo.profilePic}` : undefined}/>
                <div>
                    <div className='flex flex-row mb-2'>
                      <h2 className='text-xl mt-1 ml-4 mr-3'>{userInfo.firstName} {userInfo.lastName}</h2>
                      <button
                        onClick={() => setIsEditingProfile(true)}
                        className='px-2 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-sm font-medium'
                      >
                        Edit Profile
                      </button>
                    </div>
                    <div className='flex flex-row ml-4 mb-2'>
                        <img src="src/assets/pin.svg" alt="location"/>
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
        <div className='bg-white h-fit w-full p-4 drop-shadow mb-6'>
          <div className='flex flex-row w-full border-b border-gray-300 pb-2 mb-4'>
            <img src="src/assets/post.svg" alt="posts"/>
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
                      src={`http://localhost:8080${post.image}`}
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
        <div className='bg-white h-fit w-full p-4 drop-shadow mb-6'>
            <div className='flex flex-row w-full border-b border-gray-300 pb-2'>
                <img src="src/assets/badge.svg" alt="posts"/>
                <h3 className='ml-3 text-lg'>Badges</h3>
            </div>
            <BadgesDisplay unlockedBadges={unlockedBadges} />
        </div>
      </div>
      {toast && (
        <Toast badge={toast} onClose={() => setToast(null)} />
      )}
    </div>
  )
}

export default Profile
