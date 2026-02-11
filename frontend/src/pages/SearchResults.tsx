// src/pages/SearchResults.tsx
import React, { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { searchAll } from '../api/Search';
import { Bird } from '../types/Bird';
import { User } from '../types/User';
import { Group } from '../types/Group';
import { Post } from '../types/Post';
import BirdCard from '../components/features/BirdCard';
import FriendCard from '../components/features/FriendCard';
import GroupCard from '../components/features/GroupCard';
import PostCard from '../components/features/PostCard';
import { parseDate } from '../utils/dateTime';

interface SearchResults {
  birds: Bird[];
  users: User[];
  groups: Group[];
  posts: Post[];
}

type TabType = 'all' | 'birds' | 'users' | 'groups' | 'posts';

function SearchResults() {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('q') || '';
  const [results, setResults] = useState<SearchResults | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<TabType>('all');

  useEffect(() => {
    if (query) {
      setLoading(true);
      searchAll(query)
        .then((data) => {
          setResults({
            birds: data.birds || [],
            users: data.users || [],
            groups: data.groups || [],
            posts: data.posts || []
          });
        })
        .catch((err: any) => console.error('Search error:', err))
        .finally(() => setLoading(false));
    }
  }, [query]);

  if (loading) {
    return (
      <div className='flex items-center justify-center h-screen bg-[#F7F7F7]'>
        <p className='text-lg'>Searching...</p>
      </div>
    );
  }

  if (!results) {
    return (
      <div className='flex items-center justify-center h-screen bg-[#F7F7F7]'>
        <p className='text-lg'>No results found</p>
      </div>
    );
  }

  const totalResults = 
    results.birds.length + 
    results.users.length + 
    results.groups.length + 
    results.posts.length;

  return (
    <div className='min-h-screen bg-[#F7F7F7] px-16'>
      <div className='flex flex-row h-full'>
        {/* Left Sidebar - Filters/Categories */}
        <div className='basis-1/4 m-6 mr-0'>
          <div className='bg-white h-fit w-full p-4 drop-shadow sticky top-6'>
            <div className='border-b border-gray-300 pb-3 mb-4'>
              <h2 className='text-xl font-semibold'>Filters</h2>
              <p className='text-sm text-gray-600 mt-1'>
                {totalResults} results for "{query}"
              </p>
            </div>

            <div className='flex flex-col gap-2'>
              <button
                onClick={() => setActiveTab('all')}
                className={`text-left px-3 py-2 rounded transition ${
                  activeTab === 'all' 
                    ? 'bg-blue-50 text-blue-600 font-medium' 
                    : 'hover:bg-gray-50'
                }`}
              >
                All Results ({totalResults})
              </button>
              
              <button
                onClick={() => setActiveTab('birds')}
                className={`text-left px-3 py-2 rounded transition ${
                  activeTab === 'birds' 
                    ? 'bg-blue-50 text-blue-600 font-medium' 
                    : 'hover:bg-gray-50'
                }`}
              >
                <div className='flex items-center gap-2'>
                  <img src="/src/assets/bird.svg" alt="birds" className='w-4 h-4'/>
                  Birds ({results.birds.length})
                </div>
              </button>

              <button
                onClick={() => setActiveTab('users')}
                className={`text-left px-3 py-2 rounded transition ${
                  activeTab === 'users' 
                    ? 'bg-blue-50 text-blue-600 font-medium' 
                    : 'hover:bg-gray-50'
                }`}
              >
                <div className='flex items-center gap-2'>
                  <img src="/src/assets/person.svg" alt="users" className='w-4 h-4'/>
                  Users ({results.users.length})
                </div>
              </button>

              <button
                onClick={() => setActiveTab('groups')}
                className={`text-left px-3 py-2 rounded transition ${
                  activeTab === 'groups' 
                    ? 'bg-blue-50 text-blue-600 font-medium' 
                    : 'hover:bg-gray-50'
                }`}
              >
                <div className='flex items-center gap-2'>
                  <img src="/src/assets/groups.svg" alt="groups" className='w-4 h-4'/>
                  Groups ({results.groups.length})
                </div>
              </button>

              <button
                onClick={() => setActiveTab('posts')}
                className={`text-left px-3 py-2 rounded transition ${
                  activeTab === 'posts' 
                    ? 'bg-blue-50 text-blue-600 font-medium' 
                    : 'hover:bg-gray-50'
                }`}
              >
                <div className='flex items-center gap-2'>
                  <img src="/src/assets/post.svg" alt="posts" className='w-4 h-4'/>
                  Posts ({results.posts.length})
                </div>
              </button>
            </div>
          </div>
        </div>

        {/* Main Results Area */}
        <div className='basis-3/4 m-6'>
          {/* Birds Section */}
          {(activeTab === 'all' || activeTab === 'birds') && results.birds.length > 0 && (
            <div className='bg-white p-4 drop-shadow mb-6'>
              <div className='flex items-center border-b border-gray-300 pb-3 mb-4'>
                <img src="/src/assets/bird.svg" alt="birds" className='w-5 h-5'/>
                <h2 className='text-xl ml-3'>Birds</h2>
              </div>
              <div className='space-y-2'>
                {results.birds.map((bird) => (
                  <BirdCard key={bird.id} bird={bird} />
                ))}
              </div>
            </div>
          )}

          {/* Users Section */}
          {(activeTab === 'all' || activeTab === 'users') && results.users.length > 0 && (
            <div className='bg-white p-4 drop-shadow mb-6'>
              <div className='flex items-center border-b border-gray-300 pb-3 mb-4'>
                <img src="/src/assets/person.svg" alt="users" className='w-5 h-5'/>
                <h2 className='text-xl ml-3'>Users</h2>
              </div>
              <div className='space-y-2'>
                {results.users.map((user) => (
                  <FriendCard 
                    key={user.id} 
                    friend={{
                      id: user.id,
                      name: user.username,
                      profilePhoto: user.profilePic
                    }} 
                  />
                ))}
              </div>
            </div>
          )}

          {/* Groups Section */}
          {(activeTab === 'all' || activeTab === 'groups') && results.groups.length > 0 && (
            <div className='bg-white p-4 drop-shadow mb-6'>
              <div className='flex items-center border-b border-gray-300 pb-3 mb-4'>
                <img src="/src/assets/groups.svg" alt="groups" className='w-5 h-5'/>
                <h2 className='text-xl ml-3'>Groups</h2>
              </div>
              <div className='space-y-2'>
                {results.groups.map((group) => (
                  <GroupCard key={group.id.toString()} group={group} />
                ))}
              </div>
            </div>
          )}

          {/* Posts Section */}
          {(activeTab === 'all' || activeTab === 'posts') && results.posts.length > 0 && (
            <div className='bg-white p-4 drop-shadow mb-6'>
              <div className='flex items-center border-b border-gray-300 pb-3 mb-4'>
                <img src="/src/assets/post.svg" alt="posts" className='w-5 h-5'/>
                <h2 className='text-xl ml-3'>Posts</h2>
              </div>
              <div className='space-y-4'>
                {results.posts.map((post) => (
                  <Link key={post.id?.toString()} to={`/sightings/${post.id.toString()}`}>
                    <PostCard
                      description={post.header}
                      author={post.user.username}
                      dateTime={parseDate(post.timestamp)}
                      location={post.tags?.location}
                      likes={post.likes.length}
                      comments={post.comments.length}
                    />
                  </Link>
                ))}
              </div>
            </div>
          )}

          {/* No Results Message */}
          {activeTab === 'all' && totalResults === 0 && (
            <div className='bg-white p-8 drop-shadow text-center'>
              <p className='text-gray-600 text-lg'>No results found for "{query}"</p>
              <p className='text-gray-500 text-sm mt-2'>Try searching with different keywords</p>
            </div>
          )}

          {activeTab === 'birds' && results.birds.length === 0 && (
            <div className='bg-white p-8 drop-shadow text-center'>
              <p className='text-gray-600'>No birds found</p>
            </div>
          )}

          {activeTab === 'users' && results.users.length === 0 && (
            <div className='bg-white p-8 drop-shadow text-center'>
              <p className='text-gray-600'>No users found</p>
            </div>
          )}

          {activeTab === 'groups' && results.groups.length === 0 && (
            <div className='bg-white p-8 drop-shadow text-center'>
              <p className='text-gray-600'>No groups found</p>
            </div>
          )}

          {activeTab === 'posts' && results.posts.length === 0 && (
            <div className='bg-white p-8 drop-shadow text-center'>
              <p className='text-gray-600'>No posts found</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default SearchResults;