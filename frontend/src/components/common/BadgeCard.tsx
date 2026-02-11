import { CheckCircle } from 'lucide-react';
import { Badge } from '../../types/Badge';

interface BadgeCardProps {
  badge: Badge;
  unlocked: boolean;
}

export function BadgeCard({ badge, unlocked }: BadgeCardProps) {
  return (
    <div
      className={`flex flex-col items-center p-4 rounded-lg border-2 transition-all ${
        unlocked
          ? 'border-blue-400 bg-blue-50'
          : 'border-gray-200 bg-gray-50 opacity-50'
      }`}
    >
      <div className={`text-4xl mb-2 ${unlocked ? '' : 'grayscale'}`}>
        {badge.icon}
      </div>
      <p className='font-bold text-sm text-center text-gray-900'>{badge.name}</p>
      <p className='text-xs text-gray-600 text-center mt-1'>{badge.description}</p>
      {unlocked && (
        <div className='mt-2 flex items-center gap-1 text-blue-600'>
          <CheckCircle size={16} />
          <span className='text-xs font-medium'>Unlocked</span>
        </div>
      )}
    </div>
  );
}