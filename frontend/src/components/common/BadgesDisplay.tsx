import { BadgeCard } from './BadgeCard';
import { BADGES } from '../../utils/badgeUtils';

interface BadgesDisplayProps {
  unlockedBadges: string[];
}

export function BadgesDisplay({ unlockedBadges }: BadgesDisplayProps) {
  return (
    <div className='bg-white h-fit w-full p-4'>
      <div className='grid grid-cols-2 gap-3'>
        {Object.values(BADGES).map((badge) => (
          <BadgeCard
            key={badge.id}
            badge={badge}
            unlocked={unlockedBadges.includes(badge.id)}
          />
        ))}
      </div>
    </div>
  );
}