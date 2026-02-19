import { Link } from 'react-router-dom';
import { defaultProfilePicUrl, resolveMediaUrl } from '../../utils/mediaUrl';

type Sizes = 'sm' | 'md' | 'lg';

interface ProfileIconProps {
    size: Sizes;
    src?: string;
    userId?: string;
    clickable?: boolean;
}

function ProfileIcon({ size, src, userId, clickable = false }: ProfileIconProps) {
  let imageSrc = defaultProfilePicUrl();
  
  if (src && src.trim() !== "") {
    imageSrc = resolveMediaUrl(src);
  }
  
  const sizeClasses = {
    sm: 'w-10 h-10',
    md: 'w-14 h-14',
    lg: 'w-28 h-28'
  }

  const icon = (
    <div className={`${sizeClasses[size]} rounded-full bg-gray-500 border border-gray-300 overflow-hidden ${clickable ? 'cursor-pointer hover:opacity-80 transition-opacity' : ''}`}>
      <img src={imageSrc} alt="profile" className='w-full h-full object-cover'/>
    </div>
  );

  if (clickable && userId) {
    return <Link to={`/profile/${userId}`}>{icon}</Link>;
  }

  return icon;
}

export default ProfileIcon;