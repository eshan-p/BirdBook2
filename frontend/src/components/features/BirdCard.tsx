import React from 'react'
import { Link } from 'react-router-dom'
import { Bird } from '../../types/Bird'
import { getBirdImageUrl } from '../../utils/imageUtils'

function BirdCard({ bird }: { bird: Bird }) {
  const birdId = bird.id || bird._id;

  if (!birdId) {
    console.error("Bird missing ID:", bird);
    return null;
  }

  return (
    <Link
      to={`/birds/${birdId}`}
      className="flex items-center gap-3 p-2 rounded hover:bg-gray-100 cursor-pointer"
    >
      <img
        src={getBirdImageUrl(bird.imageURL) || "/placeholder-bird.png"}
        alt={bird.commonName}
        className="w-12 h-12 rounded object-cover"
      />
      <p className="opacity-75 text-base">{bird.commonName}</p>
    </Link>
  );
}

export default BirdCard;
