import React from 'react';
import { Link } from 'react-router-dom';

export default function Landing() {
  const handleNavigation = (path: string) => {
    window.location.href = path;
  };

  return (
    <div className='min-h-screen bg-white'>
      <nav className='fixed w-full top-0 bg-white drop-shadow z-40'>
        <div className='max-w-6xl mx-auto px-6 py-4 flex items-center justify-between'>
          <div className='flex items-center gap-2'>
            <button 
              onClick={() => ('')}
              className='w-10 h-10 flex items-center justify-center bg-gray-300 text-white font-bold rounded transition-colors shrink-0'
            >
              <img src="/sparrow-svgrepo-com.svg" alt="logo" className='w-6 h-6'/>
            </button>
            <span className='text-lg font-semibold text-gray-900'>BirdBook</span>
          </div>
          <div className='flex items-center gap-4'>
            <button
              onClick={() => handleNavigation('/login')}
              className='text-gray-700 hover:text-blue-600 font-medium text-sm transition-colors'
            >
              Log In
            </button>
            <button
              onClick={() => handleNavigation('/signup')}
              className='px-5 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium text-sm transition-colors'
            >
              Sign Up
            </button>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className='pt-48 pb-20 px-6'>
        <div className='max-w-4xl mx-auto text-center'>
          <h1 className='text-5xl font-bold text-gray-900 mb-6 leading-tight'>
            Track birds, make friends, <span className='text-blue-600'>and more</span>
          </h1>
          <p className='text-lg text-gray-600 mb-8 max-w-2xl mx-auto leading-relaxed'>
            Join a community of bird enthusiasts. Log your sightings, identify species, connect with other birdwatchers, and build your personal ornithological record.
          </p>
          <button
            onClick={() => handleNavigation('/signup')}
            className='px-8 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-semibold text-base transition-colors'
          >
            Get Started
          </button>
          <div className="auth-link mt-0">
            Or <Link to="/feed">continue as guest</Link>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className='py-20 px-6 bg-gray-50'>
        <div className='max-w-5xl mx-auto'>
          <h2 className='text-3xl font-bold text-gray-900 text-center mb-16'>What you can do</h2>
          
          <div className='grid grid-cols-1 md:grid-cols-3 gap-8'>
            {/* Feature 1 */}
            <div className='bg-white p-8 rounded-lg drop-shadow hover:drop-shadow-lg transition-shadow'>
              <div className='w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4'>
                <img src="src/assets/bird.svg" alt='groups' className='w-4 h-4'/>
              </div>
              <h3 className='text-lg font-semibold text-gray-900 mb-2'>Log Sightings</h3>
              <p className='text-gray-600'>Record where and when you spot birds with photos, descriptions, and location data.</p>
            </div>

            {/* Feature 2 */}
            <div className='bg-white p-8 rounded-lg drop-shadow hover:drop-shadow-lg transition-shadow'>
              <div className='w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4'>
                <img src="src/assets/search.svg" alt='groups' className='w-4 h-4'/>
              </div>
              <h3 className='text-lg font-semibold text-gray-900 mb-2'>Discover Species</h3>
              <p className='text-gray-600'>Browse a comprehensive bird database and learn more about species in your area.</p>
            </div>

            {/* Feature 3 */}
            <div className='bg-white p-8 rounded-lg drop-shadow hover:drop-shadow-lg transition-shadow'>
              <div className='w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4'>
                <img src="src/assets/groups.svg" alt='groups' className='w-6 h-6'/>
              </div>
              <h3 className='text-lg font-semibold text-gray-900 mb-2'>Connect & Share</h3>
              <p className='text-gray-600'>Join groups, follow friends, and share your finds with fellow bird enthusiasts.</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}