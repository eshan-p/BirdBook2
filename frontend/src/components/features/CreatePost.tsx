import React, { useState } from 'react'
import ProfileIcon from '../common/ProfileIcon'
import PostFormCard from '../common/PostFormCard';
import { Image, MapPin } from 'lucide-react';

interface CreatePostProps {
  onPostCreated?: () => void;
}

function CreatePost({ onPostCreated }: CreatePostProps) {
  const [isFormOpen, setIsFormOpen] = useState<boolean>(false);

  const openForm = () => setIsFormOpen(true);
  const closeForm = () => setIsFormOpen(false);

  return (
    <>
      <div className='w-full mb-6 bg-white rounded-lg drop-shadow'>
        <div className='p-4 border-b border-gray-200'>
          <div className='flex gap-4'>
            <div className='shrink-0'>
              <ProfileIcon size='md'/>
            </div>
            <button
              onClick={openForm}
              className='flex-1 px-4 py-3 bg-gray-100 rounded-full text-gray-600 hover:bg-gray-200 transition-colors text-left font-medium'
            >
              Log a new bird sighting...
            </button>
          </div>
        </div>
      </div>
      {isFormOpen && <PostFormCard onClose={closeForm} onPostCreated={onPostCreated}/>}
    </>
  )
}

export default CreatePost