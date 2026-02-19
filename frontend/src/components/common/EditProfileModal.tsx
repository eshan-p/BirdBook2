import React, { ChangeEvent, useState } from 'react'
import { User } from '../../types/User'
import { resolveApiUrl, resolveMediaUrl } from '../../utils/mediaUrl'

function EditProfileModal({user, onClose, onSave} : {user: User, onClose: () => void, onSave: (arg0: User) => void}) {
  const [firstName, setFirstName] = useState(user.firstName || '');
  const [lastName, setLastName] = useState(user.lastName || '');
  const [location, setLocation] = useState(
    typeof user.location === 'string'
      ? user.location
      : (user.location
          ? `${user.location.latitude},${user.location.longitude}`
          : '')
  );
  const [profilePhoto, setProfilePhoto] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState(resolveMediaUrl(user.profilePic));
  const [loading, setLoading] = useState(false);

  const handlePhotoChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if(file) {
        setProfilePhoto(file);
        const reader = new FileReader();
        reader.onloadend = () => setPreviewUrl(reader.result as string);
        reader.readAsDataURL(file);
    }
  }

  const handleSubmit = async () => {
    setLoading(true);

    const userPayload = {
      username: user.username,
      firstName,
      lastName,
      location,
      profilePic: user.profilePic,
      role: user.role,
      posts: user.posts ?? [],
      groups: user.groups ?? [],
      friends: user.friends ?? [],
    };

    const formData = new FormData();
    formData.append('user', JSON.stringify(userPayload));
    if (profilePhoto) {
      formData.append('image', profilePhoto);
    }

    try{
        const response = await fetch(resolveApiUrl(`/users/${user.id}`), {
            method: 'PATCH',
            credentials: 'include',
            body: formData
        });

        if(response.ok){
            onSave(await response.json());
            onClose();
      } else {
        const message = await response.text();
        console.error(`Failed to update profile: ${response.status} ${message}`);
        }
    } catch (err) {
        console.error("Failed to update profile: " + err);
    } finally {
        setLoading(false);
    }
  }

  return (
    <div className='fixed inset-0 bg-black/20 flex items-center justify-center z-50'>
      <div className='bg-white rounded-2xl p-8 max-w-md w-full'>
        <h2 className='text-2xl font-bold mb-6'>Edit Profile</h2>
        
        <div className='space-y-4 mb-6'>
          <div className='flex flex-col items-center'>
            <img src={previewUrl} alt='Profile' className='w-20 h-20 rounded-full object-cover mb-3' />
            <label className='cursor-pointer'>
              <input type='file' accept='image/*' onChange={handlePhotoChange} className='hidden' />
              <span className='text-sm text-blue-600 hover:underline'>Change Photo</span>
            </label>
          </div>
          <input
            type='text'
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            placeholder='First Name'
            className='w-full px-3 py-2 border border-gray-300 rounded-lg'
          />
          <input
            type='text'
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            placeholder='Last Name'
            className='w-full px-3 py-2 border border-gray-300 rounded-lg'
          />
          <input
            type='text'
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            placeholder='Location'
            className='w-full px-3 py-2 border border-gray-300 rounded-lg'
          />
        </div>
        <div className='flex gap-3'>
          <button
            onClick={onClose}
            className='flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50'
            disabled={loading}
          >
            Cancel
          </button>
          <button
            onClick={handleSubmit}
            className='flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400'
            disabled={loading}
          >
            {loading ? 'Saving...' : 'Save'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default EditProfileModal
