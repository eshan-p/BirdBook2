import React, { useState } from 'react'
import GroupFormCard from '../common/GroupFormCard';

function CreateGroup() {
  const[isFormOpen, setIsFormOpen] = useState<boolean>(false);
  const openForm = () => setIsFormOpen(true);
  const closeForm = () => setIsFormOpen(false);

  return (
    <div>
      <button onClick={openForm} className='w-full ml-4'>
        <div className='px-2 py-2  border border-gray-300 rounded-lg hover:bg-gray-50 text-sm font-medium'>
          New Group
        </div>
      </button>
      {isFormOpen && <GroupFormCard onClose={closeForm}/>}
    </div>
  )
}

export default CreateGroup
