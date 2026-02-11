import React, { ChangeEvent, useState } from 'react'
import { useAuth } from '../../context/AuthContext';
import { addBird, updateBird } from '../../api/Birds';
import { Bird } from '../../types/Bird';

function BirdFormCard({onClose, bird, onUpdate} : {onClose: () => void, bird?: Bird, onUpdate?: (updatedBird: Bird) => void}) {
  const { user } = useAuth();
  const [commonName, setCommonName] = useState(bird?.commonName || '');
  const [scientificName, setScientificName] = useState(bird?.scientificName || '');
  const [image, setImage] = useState<File | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const isEditMode = !!bird;

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    if(!user?.id) {
      setError('You must be logged in to create a bird');
      setLoading(false);
      return;
    }

    try {
      const birdData = {
        commonName,
        scientificName,
      };
      
      if (isEditMode && bird) {
        // Edit mode - use PATCH request
        const updatedBird = await updateBird(bird.id, birdData, image || undefined);
        if (onUpdate) onUpdate(updatedBird);
        console.log("Bird updated successfully");
      } else {
        // Create mode - use POST request
        await addBird(birdData, image || undefined);
        console.log("Bird created successfully");
      }
      onClose();
      window.location.reload();
    } catch(error) {
      setError(error instanceof Error ? error.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  function handleImageChange(event: ChangeEvent<HTMLInputElement>): void {
    if (event.target.files && event.target.files[0]) {
      setImage(event.target.files[0])
    }
  };

  return (
    <div className='fixed inset-0 z-49 flex items-center justify-center bg-black/20'>
      <div className='bg-white p-8 rounded-2xl drop-shadow w-full max-w-xl m-4'>
        <h2 className='text-2xl mb-6'>{isEditMode ? 'Edit Bird' : 'Create New Bird'}</h2>
        <form onSubmit={handleSubmit}>
          <div>
            <label className='block text-sm mb-1'>
              Common Name <span className='text-red-500'>*</span>
            </label>
            <input
              type='text'
              value={commonName}
              onChange={(e) => setCommonName(e.target.value)}
              maxLength={100}
              required
              className='w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500'
              placeholder='Enter common name'
            />
            <p className='text-xs text-gray-500 mt-1 mb-1'>{commonName.length}/100 characters</p>
          </div>

          <div>
            <label className='block text-sm mb-1'>
              Scientific Name <span className='text-red-500'>*</span>
            </label>
            <input
              type='text'
              value={scientificName}
              onChange={(e) => setScientificName(e.target.value)}
              maxLength={100}
              required
              className='w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500'
              placeholder='Enter scientific name'
            />
            <p className='text-xs text-gray-500 mt-1 mb-1'>{scientificName.length}/100 characters</p>
          </div>

          <div>
            <label className='block text-sm mb-1'>
              Image
            </label>
            <input
              type='file'
              accept='image/*'
              onChange={handleImageChange}
              className='w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500'
            />
            {image && (
              <p className='text-xs text-gray-600 mt-1'>Selected: {image.name}</p>
            )}
          </div>

          {error && (
            <div className='p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm'>
              {error}
            </div>
          )}

          <div className='flex gap-3 pt-4'>
            <button
              type='button'
              onClick={onClose}
              disabled={loading}
              className='flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50'
            >
              Cancel
            </button>
            <button
              type='submit'
              disabled={loading || !commonName.trim() || !scientificName.trim()}
              className='flex-1 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:cursor-not-allowed'
            >
              {loading ? (isEditMode ? 'Updating...' : 'Creating...') : (isEditMode ? 'Update Bird' : 'Create Bird')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default BirdFormCard
