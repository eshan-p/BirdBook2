import React, { useEffect, useState } from 'react'
import { Group } from '../../types/Group'
import { arrayToCoords, reverseCoordsToCityState } from '../../utils/geolocation';
import ProfileIcon from '../common/ProfileIcon';
import { useNavigate } from 'react-router-dom';

interface GroupCardProps {
  group: Group;
  onJoin?: () => void;
  onLeave?: () => void;
  onDelete?: () => void;
}

function GroupCard({ group, onJoin, onLeave, onDelete }: GroupCardProps) {
    const navigate = useNavigate();

    return (
    <div 
      onClick={() => navigate(`/groups/${group.id}`)}
      className="flex flex-row items-center justify-between p-4 hover:bg-gray-100 rounded cursor-pointer"
    >
      <div className="flex flex-row items-center">
        <div>
          <ProfileIcon size='sm' />
        </div>
        <div className='ml-3'>
          <h3 className="text-sm font-semibold text-gray-900 leading-tight">
            {group.name}
          </h3>
          {group.description && (
            <p className='text-xs text-gray-600 mt-0.5'>{group.description}</p>
          )}
          <p className='text-xs text-gray-500 mt-0.5'>{group.members?.length || 0} followers</p>
        </div>
      </div>
    </div>
  );
}

export default GroupCard