import React, { useState } from 'react'
import BirdFormCard from '../common/BirdFormCard';

function CreateBird() {
  const[isFormOpen, setIsFormOpen] = useState<boolean>(false);
  const openForm = () => setIsFormOpen(true);
  const closeForm = () => setIsFormOpen(false);

  return (
    <div>
      <button onClick={openForm} className='w-full'>
        <div className='px-2 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-sm font-medium'>
          New Bird
        </div>
      </button>
      {isFormOpen && <BirdFormCard onClose={closeForm}/>}
    </div>
  )
}

export default CreateBird
