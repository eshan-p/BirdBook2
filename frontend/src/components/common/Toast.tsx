import React from 'react';
import { X } from 'lucide-react';
import { Badge } from '../../types/Badge';

interface ToastProps {
  badge: Badge;
  onClose: () => void;
}

export function Toast({ badge, onClose }: ToastProps) {
  React.useEffect(() => {
    const timer = setTimeout(onClose, 3000);
    return () => clearTimeout(timer);
  }, [onClose]);

  return (
    <div className='fixed bottom-4 right-4 bg-white rounded-lg shadow-lg p-4 flex items-center gap-3 animate-pulse z-50'>
      <div className='text-3xl'>{badge.icon}</div>
      <div>
        <p className='font-bold text-gray-900'>Badge Unlocked!</p>
        <p className='text-sm text-gray-600'>{badge.name}</p>
      </div>
      <button onClick={onClose} className='ml-2'>
        <X size={18} className='text-gray-400 hover:text-gray-600' />
      </button>
    </div>
  );
}