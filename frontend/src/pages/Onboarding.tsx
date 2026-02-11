import React, { ChangeEvent, useState } from 'react';

export default function Onboarding() {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [location, setLocation] = useState('');
  const [profilePhoto, setProfilePhoto] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string>('');
  const [isSearching, setIsSearching] = useState(false);
  const [locationSuggestions, setLocationSuggestions] = useState<any[]>([]);
  const [selectedLocation, setSelectedLocation] = useState<string>('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handlePhotoChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setProfilePhoto(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleLocationSearch = async (value: string) => {
    setLocation(value);
    if (value.length < 2) {
      setLocationSuggestions([]);
      return;
    }

    setIsSearching(true);
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(value)}&limit=5`,
        { headers: { 'Accept': 'application/json' } }
      );
      const results = await response.json();
      setLocationSuggestions(results);
    } catch (err) {
      console.error('Location search error:', err);
      setLocationSuggestions([]);
    } finally {
      setIsSearching(false);
    }
  };

  const selectLocation = (suggestion: any) => {
    setLocation(suggestion.display_name);
    setSelectedLocation(suggestion.display_name);
    setLocationSuggestions([]);
  };

  const handleSubmit = async () => {
    setError('');
    setLoading(true);

    if (!firstName.trim() || !lastName.trim()) {
      setError('Please enter your first and last name');
      setLoading(false);
      return;
    }

    if (!selectedLocation) {
      setError('Please select a location');
      setLoading(false);
      return;
    }

    try {
      const formData = new FormData();
      formData.append('firstName', firstName);
      formData.append('lastName', lastName);
      formData.append('location', selectedLocation);
      if (profilePhoto) {
        formData.append('profilePhoto', profilePhoto);
      }

      const response = await fetch('http://localhost:8080/users/onboard', {
        method: 'POST',
        credentials: 'include',
        body: formData,
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to complete onboarding');
      }

      window.location.href = '/feed';
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className='min-h-screen from-blue-50 to-blue-100 flex items-center justify-center px-4 py-8'>
      <div className='w-full max-w-md'>
        {/* Header */}
        <div className='text-center mb-8'>
        <button 
          onClick={() => ('')}
          className='w-10 h-10 flex items-center justify-center bg-gray-300 text-white font-bold rounded hover:bg-white transition-colors shrink-0'
        >
          <img src="/sparrow-svgrepo-com.svg" alt="logo" className='w-6 h-6'/>
        </button>
          <h1 className='text-3xl font-bold text-gray-900 mb-2'>Welcome to BirdBook</h1>
          <p className='text-gray-600'>Complete your profile to get started</p>
        </div>

        {/* Form Card */}
        <div className='bg-white rounded-2xl drop-shadow-lg p-8'>
          <div className='space-y-5'>
            {/* Name Section */}
            <div className='space-y-4'>
              <h2 className='text-lg font-semibold text-gray-900'>Basic Information</h2>
              
              <div className='grid grid-cols-2 gap-3'>
                <div>
                  <label className='block text-sm font-medium text-gray-700 mb-1'>
                    First Name
                  </label>
                  <input
                    type='text'
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    placeholder='John'
                    className='w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500'
                  />
                </div>
                <div>
                  <label className='block text-sm font-medium text-gray-700 mb-1'>
                    Last Name
                  </label>
                  <input
                    type='text'
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    placeholder='Doe'
                    className='w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500'
                  />
                </div>
              </div>
            </div>

            {/* Location Section */}
            <div className='space-y-4 border-t border-gray-200 pt-5'>
              <h2 className='text-lg font-semibold text-gray-900'>Location</h2>
              
              <div className='relative'>
                <label className='block text-sm font-medium text-gray-700 mb-1'>
                  Where are you based?
                </label>
                <input
                  type='text'
                  value={location}
                  onChange={(e) => handleLocationSearch(e.target.value)}
                  placeholder='Search for a city or region...'
                  className='w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500'
                />
                
                {isSearching && (
                  <p className='text-xs text-gray-500 mt-2'>Searching...</p>
                )}

                {locationSuggestions.length > 0 && (
                  <div className='absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-48 overflow-y-auto'>
                    {locationSuggestions.map((suggestion, index) => (
                      <button
                        key={index}
                        onClick={() => selectLocation(suggestion)}
                        className='w-full text-left px-3 py-2 hover:bg-blue-50 border-b last:border-b-0 transition-colors'
                      >
                        <div className='text-sm font-medium text-gray-900'>
                          {suggestion.display_name.split(',')[0]}
                        </div>
                        <div className='text-xs text-gray-500'>
                          {suggestion.display_name.split(',').slice(-2).join(',')}
                        </div>
                      </button>
                    ))}
                  </div>
                )}
              </div>

              {selectedLocation && (
                <div className='flex items-center gap-2 p-2 bg-blue-50 rounded-lg'>
                  <svg className='w-4 h-4 text-blue-600 flex-shrink-0' fill='currentColor' viewBox='0 0 20 20'>
                    <path fillRule='evenodd' d='M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z' clipRule='evenodd' />
                  </svg>
                  <p className='text-sm text-blue-900'>{selectedLocation}</p>
                </div>
              )}
            </div>

            {/* Profile Photo Section */}
            <div className='space-y-4 border-t border-gray-200 pt-5'>
              <h2 className='text-lg font-semibold text-gray-900'>Profile Photo</h2>
              
              <div className='flex flex-col items-center'>
                {previewUrl ? (
                  <div className='relative mb-4'>
                    <img
                      src={previewUrl}
                      alt='Profile preview'
                      className='w-24 h-24 rounded-full object-cover border-4 border-blue-200'
                    />
                    <button
                      onClick={() => {
                        setProfilePhoto(null);
                        setPreviewUrl('');
                      }}
                      className='absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600 transition-colors text-sm font-bold'
                    >
                      ✕
                    </button>
                  </div>
                ) : (
                  <div className='w-24 h-24 rounded-full bg-gray-200 flex items-center justify-center mb-4'>
                    <svg className='w-10 h-10 text-gray-400' fill='currentColor' viewBox='0 0 20 20'>
                      <path fillRule='evenodd' d='M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z' clipRule='evenodd' />
                    </svg>
                  </div>
                )}

                <label className='cursor-pointer'>
                  <input
                    type='file'
                    accept='image/*'
                    onChange={handlePhotoChange}
                    className='hidden'
                  />
                  <span className='px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors inline-block font-medium text-sm'>
                    {previewUrl ? 'Change Photo' : 'Upload Photo'}
                  </span>
                </label>
                <p className='text-xs text-gray-500 mt-2'>Optional • JPG or PNG, max 5MB</p>
              </div>
            </div>

            {/* Error Message */}
            {error && (
              <div className='p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm'>
                {error}
              </div>
            )}

            {/* Submit Button */}
            <button
              onClick={handleSubmit}
              disabled={loading}
              className='w-full py-3 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors mt-6'
            >
              {loading ? 'Completing Setup...' : 'Get Started'}
            </button>
          </div>
        </div>

        {/* Footer */}
        <p className='text-center text-gray-600 text-sm mt-6'>
          You can update this information in your profile settings anytime
        </p>
      </div>
    </div>
  );
}