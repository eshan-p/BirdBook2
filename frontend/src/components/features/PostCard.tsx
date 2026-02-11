import React, { useEffect, useState } from 'react'
import { Point } from 'geojson'
import { pointToCoords, reverseCoordsToCityState } from '../../utils/geolocation';
import ProfileIcon from '../common/ProfileIcon';
import { getTimeSince } from '../../utils/dateTime';
import { Post } from '../../types/Post';
import { getSightings } from '../../api/Sightings';
import { User } from '../../types/User';
import { getUserById } from '../../api/Users';

interface PostCardProps {
  description: string;
  author: string;
  authorId: string;
  authorProfilePic?: string;
  dateTime: Date;
  location?: {latitude:number,longitude:number};
  likes: number;
  comments: number;
  image?: string | null;
}

interface NominatimResponse { 
  display_name: string;
  address: {
    city?: string;
    state?: string;
  }
}


function PostCard({description, author, authorId, authorProfilePic, dateTime, location, likes, comments, image}: PostCardProps) {
  const [locationName, setLocationName] = useState<String>('Loading...');
  const [timeSince, setTimeSince] = useState<String>('');
  //const [user, setUser] = useState<User | null>(null);
  
  /*
    useEffect(() => {
      if (!author) return;
  
      getUserById(author)
        .then(setUser)
        .catch(() => setUser(null));
    }, []); */

  /*
  useEffect(() => {
    const coords = pointToCoords(location);
    reverseCoordsToCityState(coords).then(setLocationName);
  }, [location.coordinates]); */

useEffect(() => {
  if (!location) return;

  let raw = location as any;
  //console.log("RAW location tag:", raw);

  // 🔑 FIX: handle stringified JSON
  if (typeof raw === "string") {
    try {
      raw = JSON.parse(raw);
    } catch {
      console.error("Location is not valid JSON:", raw);
      return;
    }
  }

  const latitude = Number(raw.latitude);
  const longitude = Number(raw.longitude);

  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
    console.error("Invalid coordinates AFTER parsing:", raw);
    return;
  }

  reverseCoordsToCityState({ latitude, longitude })
    .then(setLocationName)
    .catch((err) => {
      console.error("Reverse geocode failed:", err);
      //setLocationName(null);
    });
  }, []);

  useEffect(() => {
    setTimeSince(getTimeSince(dateTime));
    const interval = setInterval(() => {
      setTimeSince(getTimeSince(dateTime));
    }, 60000);
    return () => clearInterval(interval)
  }, [dateTime])

  return (
    <div className='w-full bg-white p-4 drop-shadow hover:drop-shadow-lg'>
      <div className='flex flex-row mb-3 text-left items-start'>
        <ProfileIcon size="md" src={authorProfilePic} userId={authorId}/>
        <div className='ml-3'>
          <h3 className='font-bold text-base'>{author ? author : "Unknown user"}</h3>
          <p className='text-sm/3 opacity-85'>{locationName}</p>
          <p className='text-sm/6 opacity-85'>{timeSince}</p>
        </div>
      </div>
      <p className='text-md/5 text-left'>{description}</p>
      {image && (
        <div className='mt-3'>
          <img 
            src={`http://localhost:8080${image}`} 
            alt={description} 
            className='w-full rounded-lg object-cover max-h-96'
          />
        </div>
      )}
      <div className='flex flex-row mt-2'>
        <div className='flex flex-row items-center mr-3'>
          <img src="/src/assets/heart.png" alt="like" className='w-5 h-5 mr-1'/>
          <p>{likes}</p>
        </div>
        <div className='flex flex-row items-center'>
          <img src="/src/assets/comment.png" alt="like" className='w-5 h-5 mr-1'/>
          <p>{comments}</p>
        </div>
      </div>
    </div>
  )
}

export default PostCard;
