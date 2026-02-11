import React, { useEffect, useState, useRef } from 'react'
import SearchBar from '../common/SearchBar'
import ProfileIcon from '../common/ProfileIcon'
import { useNavigate } from "react-router-dom";
import { useAuth } from '../../context/AuthContext';
import { getUserById } from '../../api/Users';
import { User } from '../../types/User';
import { isBasicUser, isSuperUser,isAdmin } from '../../utils/roleUtils';

function Header() {
  const navigate = useNavigate();
  const { user: authUser, logout } = useAuth();
  const [userData, setUserData] = useState<User | null>(null);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (authUser?.id) {
      getUserById(authUser.id)
        .then(setUserData)
        .catch(console.error);
    }
  }, [authUser?.id]);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsMenuOpen(false);
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleViewProfile = () => {
    setIsMenuOpen(false);
    navigate('/profile');
  };

  const handleEditProfile = () => {
    setIsMenuOpen(false);
    navigate('/profile?edit=true');
  };

  const handleLogout = async () => {
    setIsMenuOpen(false);
    await logout();
    navigate('/login');
  };

  const protectedNavItems = [
    { label: 'Home', path: '/feed' },
    { label: 'Groups', path: '/groups' },
    { label: 'Friends', path: '/friends' },
    { label: 'Birds', path: '/birds' },
    { label: 'Users', path: '/users' }
  ];

  const navItems = authUser ? protectedNavItems : [];

  return (
    <div className='bg-white flex flex-row justify-between items-center h-16 px-22 drop-shadow sticky top-0 z-50'>
      <div className='basis-2/6 flex flex-row justify-start min-w-0 gap-4'> 
        <button 
          onClick={() => navigate('/')}
          className='w-10 h-10 flex items-center justify-center bg-gray-300 text-white font-bold rounded hover:bg-white transition-colors shrink-0'
        >
          <img src="/sparrow-svgrepo-com.svg" alt="logo" className='w-6 h-6'/>
        </button>
        <SearchBar searchType='all' placeholder='Search birds, users, groups...'/>
      </div>

      <div className='basis-3/6 flex flex-row justify-center  item-center gap-12 shrink-0 w-full'>
        {navItems.filter(item =>
            !item.requiresAuth || isBasicUser(authUser?.role) || isAdmin(authUser?.role) || isSuperUser(authUser?.role)
          )
          .map((item) => (
          <button 
            key={item.path}
            onClick={() => navigate(item.path)}
            className='text-base font-medium text-gray-700 hover:text-blue-600 transition-colors p-4'
          >
            {item.label}
          </button>
        ))}
      </div>

      <div className='basis-1/6 flex flex-row shrink-0 relative justify-end items-center' ref={menuRef}>
        {authUser ? (
          <button
            onClick={() => setIsMenuOpen(!isMenuOpen)}
            className='hover:opacity-80 transition-opacity '
          >
            <div className='flex gap-2 items-center text-base font-medium text-gray-700 px-4 py-1 border border-gray-300 rounded-lg hover:bg-gray-50'>
              <h2>{userData?.username}</h2>
              <ProfileIcon size='sm' src={userData?.profilePic ? `http://localhost:8080${userData.profilePic}` : undefined}/>
            </div>
          </button>
        ) : (
          <button
            onClick={() => navigate('/login')}
            className='px-4 py-2 bg-blue-600 text-white font-medium rounded hover:bg-blue-700 transition-colors text-sm'
          >
            Log In
          </button>
        )}

        {authUser && isMenuOpen && (
          <div className='absolute top-14 right-0 bg-white rounded-lg drop-shadow-lg overflow-hidden w-48 z-10'>
            <button
              onClick={handleViewProfile}
              className='w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-100 transition-colors flex items-center gap-2 border-b border-gray-200'
            >
              <img src="src/assets/person.svg" alt="profile" className='w-5 h-5'/>
              View Profile
            </button>
            <button
              onClick={handleEditProfile}
              className='w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-100 transition-colors flex items-center gap-2 border-b border-gray-200'
            >
              <svg className='w-4 h-4' fill='none' stroke='currentColor' viewBox='0 0 24 24'>
                <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2} d='M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z' />
              </svg>
              Edit Profile
            </button>
            <button
              onClick={handleLogout}
              className='w-full text-left px-4 py-3 text-red-600 hover:bg-red-50 transition-colors flex items-center gap-2'
            >
              <svg className='w-4 h-4' fill='none' stroke='currentColor' viewBox='0 0 24 24'>
                <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2} d='M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1' />
              </svg>
              Log Out
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

export default Header