import { useEffect, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { Bird } from "../types/Bird";
import { getAllBirds, deleteBird } from "../api/Birds";
import { useAuth } from "../context/AuthContext";
import { isSuperUser } from "../utils/roleUtils";
import BirdFormCard from "../components/common/BirdFormCard";

export default function BirdDetail() {
  const { birdId } = useParams<{ birdId: string }>();
  const [bird, setBird] = useState<Bird | null>(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!birdId) return;

    getAllBirds()
      .then(birds => {
        const found = birds.find(b => b.id === birdId);
        setBird(found || null);
      })
      .finally(() => setLoading(false));
  }, [birdId]);

  const handleUpdateBird = (updatedBird: Bird) => {
    setBird(updatedBird);
    setIsEditing(false);
  };

  const handleDeleteBird = async () => {
    if (!birdId || !user?.id) return;
    if (!confirm('Are you sure you want to delete this bird? This action cannot be undone.')) return;
    
    try {
      await deleteBird(birdId);
      navigate('/birds');
    } catch (err: any) {
      console.error(err);
      alert(err.message || 'Failed to delete bird');
    }
  };

  if (loading) return <p className="p-6">Loading bird...</p>;

  if (!bird) {
    return (
      <div className="p-6">
        <p className="text-gray-600">Bird not found.</p>
        <Link to="/birds" className="text-blue-600 underline">
          Back to Birds
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto p-6 bg-white drop-shadow">
      <Link to="/birds" className="text-blue-600 underline">
        ← Back to Birds
      </Link>

      <img
        src={bird.imageURL || "/placeholder-bird.png"}
        alt={bird.commonName}
        className="w-full h-64 object-cover rounded mt-4"
      />

      <h1 className="text-3xl font-semibold mt-4">
        {bird.commonName}
      </h1>

      {bird.scientificName && (
        <p className="text-gray-500 italic mt-1">
          {bird.scientificName}
        </p>
      )}

      {bird.location && (
        <p className="mt-4 text-sm text-gray-600">
          Location: {bird.location[1]}, {bird.location[0]}
        </p>
      )}

      {user && isSuperUser(user.role) && (
        <div className='mt-6 border-t border-gray-300 pt-4 space-y-2'>
          <button 
            onClick={() => setIsEditing(true)} 
            className='w-full px-4 py-2 bg-blue-600 text-white text-sm rounded hover:bg-blue-700'
          >
            Edit Bird
          </button>
          <button 
            onClick={handleDeleteBird} 
            className='w-full px-4 py-2 bg-red-600 text-white text-sm rounded hover:bg-red-700'
          >
            Delete Bird
          </button>
        </div>
      )}
      
      {isEditing && bird && (
        <BirdFormCard 
          onClose={() => setIsEditing(false)} 
          bird={bird}
          onUpdate={handleUpdateBird}
        />
      )}
    </div>
  );
}
