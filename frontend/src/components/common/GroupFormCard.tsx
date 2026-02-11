import React, { ChangeEvent, useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext';
import { Bird } from '../../types/Bird';
import { Group } from '../../types/Group'
import { reverseCoordsToCityState, arrayToCoords } from '../../utils/geolocation'
import { User } from '../../types/User';

function GroupFormCard({onClose, group, onUpdate} : {onClose: () => void, group?: Group, onUpdate?: (updatedGroup: Group) => void}) {
  const { user } = useAuth();
  const [name, setName] = useState(group?.name || '');
  const [description, setDescription] = useState(group?.description || '');
  const [location, setLocation] = useState<[number, number] | null>(group?.location || null);
  const [locationName, setLocationName] = useState('');
  const [locationInput, setLocationInput] = useState('');
  const [isEditingLocation, setIsEditingLocation] = useState(false);
  const [image, setImage] = useState<File | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const isEditMode = !!group;

  const BASE_URL = "http://localhost:8080";

    useEffect(() => {
        if(navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const coords: [number, number] = [
                        position.coords.latitude,
                        position.coords.longitude
                    ];
                    setLocation(coords);
                    reverseCoordsToCityState(arrayToCoords(coords)).then(setLocationName).catch(error => console.error("Error geocoding:" + error));
                },
                (error) => {
                    console.error("Error getting location:" + error);
                }
            );
        }
    }, []);

    const handleLocationSearch = async () => {
        if (!locationInput.trim()) return;

        try {
            const response = await fetch(
            `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(locationInput)}`,
            { headers: { 'Accept': 'application/json' } }
            );

            const results = await response.json();

            if (results.length > 0) {
            const result = results[0];
            const coords: [number, number] = [parseFloat(result.lat), parseFloat(result.lon)];
            setLocation(coords);
            setLocationName(locationInput);
            setIsEditingLocation(false);
            setLocationInput('');
            } else {
            setError('Location not found. Please try another search.');
            }
        } catch (err) {
            console.error("Error searching location:", err);
            setError('Failed to search location');
        }
    };
    
    const handleUseCurrentLocation = () => {
        if(navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const coords: [number, number] = [
                        position.coords.latitude,
                        position.coords.longitude
                    ];
                    setLocation(coords);
                    reverseCoordsToCityState(arrayToCoords(coords)).then(setLocationName).catch(error => console.error("Error geocoding:" + error));
                    setIsEditingLocation(false);
                },
                (error) => {
                    console.error("Error getting location:" + error);
                    setError('Failed to get current location');
                }
            );
        }
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
      e.preventDefault();
      setError('');
      setLoading(true);
      if(!user?.id) {
          setError('You must be logged in to create a group');
          setLoading(false);
          return;
      }
      try{
          if (isEditMode && group) {
              // Edit mode - use PUT request
              const response = await fetch(`${BASE_URL}/groups/${group.id}?userId=${user.id}`, {
                  method: 'PUT',
                  credentials: 'include',
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  body: JSON.stringify({ name, description })
              });
              if(!response.ok) {
                  const errorText = await response.text();
                  throw new Error(errorText || 'Failed to update group');
              }
              const updatedGroup = await response.json();
              if (onUpdate) onUpdate(updatedGroup);
              console.log("Group updated successfully");
          } else {
              // Create mode - use POST request
              const groupData = {
                  name,
                  description,
                  tags: {
                      ...(location && {
                          latitude: location[0].toString(),
                          longitude: location[1].toString()
                      })
                  },
                  members: [],
                  requests: []
              };
              const formData = new FormData();
              formData.append('group', JSON.stringify(groupData));
              formData.append('userId', user.id);
              if(image) {
                  formData.append('image', image);
              }
              const response = await fetch(`${BASE_URL}/groups`, {
                  method: 'POST',
                  credentials: 'include',
                  body: formData
              });
              if(!response.ok) {
                  const errorData = await response.json();
                  throw new Error(errorData.message || 'Failed to create group');
              }
              console.log("Group created successfully");
          }
          onClose();
          window.location.reload();
      } catch(error) {
          setError(error instanceof Error ? error.message : 'An error occured');
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
    <div className='fixed inset-0 z-49 flex items-center justify-center bg-black/20 backdrop-blur-sm"'>
        <div className='bg-white backdrop-blur-md p-8 rounded-2xl drop-shadow w-full max-w-xl m-4'>
            <h2 className='text-2xl mb-6'>{isEditMode ? 'Edit Group' : 'Create New Group'}</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label className='block text-sm mb-1'>
                        Title <span className='text-red-500'>*</span>
                    </label>

                    <input
                        type='text'
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        maxLength={50}
                        required
                        className='w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500'
                        placeholder='Enter group name'
                    />
                    <p className='text-xs text-gray-500 mt-1 mb-1'>{name.length}/50 characters</p>

                    <div>
                        <label className='block text-sm mb-1'>
                            Description <span className='text-red-500'>*</span>
                        </label>
                        <textarea
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            maxLength={300}
                            required
                            rows={4}
                            className='w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500'
                            placeholder='Group description'
                        />
                        <p className='text-xs text-gray-500 mb-1'>{description.length}/300 characters</p>
                    </div>

                    <div>
                        <label className='block text-sm mb-1'>
                            location
                        </label>
                        {!isEditingLocation ? (
                            <div className='px-3 py-2 bg-gray-50 border border-gray-300 rounded-lg text-sm flex items-center justify-between'>
                                <div>
                                    {location ? (
                                        <p className='font-medium'>{locationName}</p>
                                    ) : (
                                        <div className='text-gray-500'>Getting location...</div>
                                    )}
                                </div>
                                <button
                                    type='button'
                                    onClick={() => setIsEditingLocation(true)}
                                    className='ml-2 px-3 py-1 bg-blue-500 text-white text-xs rounded hover:bg-blue-600 whitespace-nowrap'
                                >
                                    Change
                                </button>
                            </div>
                        ) : (
                            <div className='space-y-2'>
                                <input
                                    type='text'
                                    value={locationInput}
                                    onChange={(e) => setLocationInput(e.target.value)}
                                    placeholder='Enter location (city, address, coordinates)'
                                    className='w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500'
                                    onKeyDown={(e) => e.key === 'Enter' && handleLocationSearch()}
                                />
                                <div className='flex gap-2'>
                                    <button
                                        type='button'
                                        onClick={handleLocationSearch}
                                        className='flex-1 px-3 py-2 bg-blue-500 text-white text-sm rounded hover:bg-blue-600'
                                    >
                                        Search
                                    </button>
                                    <button
                                        type='button'
                                        onClick={handleUseCurrentLocation}
                                        className='flex-1 px-3 py-2 bg-gray-500 text-white text-sm rounded hover:bg-gray-600'
                                    >
                                        Use Current
                                    </button>
                                    <button
                                        type='button'
                                        onClick={() => {
                                            setIsEditingLocation(false);
                                            setLocationInput('');
                                        }}
                                        className='flex-1 px-3 py-2 border border-gray-300 text-sm rounded hover:bg-gray-50'
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </div>
                        )}
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
                            className='flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50'
                            disabled={loading}
                        >
                            Cancel
                        </button>
                        <button
                            type='submit'
                            className='flex-1 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed'
                            disabled={loading}
                        >
                            {loading ? 'Creating...' : 'Create group'}
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
  )
}

export default GroupFormCard
