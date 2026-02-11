import React from 'react'
import ProfileIcon from '../common/ProfileIcon'
import { User } from '../../types/User'
import { Link } from 'react-router-dom';

interface ProfileCardProps {
  user?: User;
}

function ProfileCard({ user }: ProfileCardProps) {
  return (
    <div className='h-fit w-full bg-white px-4 py-6 drop-shadow flex flex-col items-center'>
        <ProfileIcon size='lg' src={user?.profilePic ? `http://localhost:8080${user.profilePic}` : undefined}/>
        <h3 className='text-xl mt-1'>{user?.firstName || 'User'} {user?.lastName || ''}</h3>
        <div className='flex flex-row items-center w-full justify-between px-3 mt-4'>
            <div className='flex flex-col items-center'>
                <p className='text-2xl font-light text-[#0700D3]'>{user?.posts?.length || 0}</p>
                <p className='text-base font-extralight'>Spottings</p>
            </div>
            <div className='flex flex-col items-center'>
                <p className='text-2xl font-light text-[#0700D3]'>{user?.friends?.length || 0}</p>
                <p className='text-base font-extralight'>Friends</p>
            </div>
            <div className='flex flex-col items-center'>
                <p className='text-2xl font-light text-[#0700D3]'>{user?.groups?.length || 0}</p>
                <p className='text-base font-extralight'>Groups</p>
            </div>
        </div>
    </div>
  )
}

export default ProfileCard
