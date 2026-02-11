import { Badge } from '../types/Badge';
import { User } from '../types/User';

//https://getemoji.com/
export const BADGES: Record<string, Badge> = {
  FIRST_SIGHTING: {
    id: 'first_sighting',
    name: 'First Flight',
    description: 'Record your first bird sighting',
    icon: '🐦'
  },
  COLLECTOR: {
    id: 'collector',
    name: 'Collector',
    description: 'Spot 10 different bird species',
    icon: '🦅'
  },
  SOCIAL_BUTTERFLY: {
    id: 'social_butterfly',
    name: 'Social Butterfly',
    description: 'Make 5 friends',
    icon: '🦋'
  },
  PHOTOGRAPHER: {
    id: 'photographer',
    name: 'Photographer',
    description: 'Get 20 likes on your sightings',
    icon: '📸'
  }
};

export function getUnlockedBadges(user: User, posts: any[]): string[] {
  const unlocked: string[] = [];
  
  if (posts.length >= 1) {
    unlocked.push('first_sighting');
  }
  
  const uniqueSpecies = new Set(
    posts
      .filter(p => p.bird)
      .map((p) => (p.bird as any))
  ).size;
  console.log(uniqueSpecies)

  if (uniqueSpecies >= 10) {
    unlocked.push('collector');
  }
  
  if (user.friends && user.friends.length >= 3) {
    unlocked.push('social_butterfly');
  }
  
  const totalLikes = posts.reduce((sum, post) => sum + (post.likes?.length || 0), 0);
  if (totalLikes >= 5) {
    unlocked.push('photographer');
  }
  
  return unlocked;
}