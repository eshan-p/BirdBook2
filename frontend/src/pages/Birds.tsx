import React, { useState, useEffect } from "react";
import ProfileCard from "../components/features/ProfileCard";
import GroupCard from "../components/features/GroupCard";
import FriendCard from "../components/features/FriendCard";
import BirdCard from "../components/features/BirdCard";
import SearchBar from "../components/common/SearchBar";
import { Group } from "../types/Group";
import { Bird } from "../types/Bird";
import { getAllBirds } from "../api/Birds";
import { useAuth } from "../context/AuthContext";
import { getUserById } from "../api/Users";
import { User } from "../types/User";
import { getUserGroups } from "../api/Groups";
import { isBasicUser, isAdmin, isSuperUser } from "../utils/roleUtils";
import CreateBird from "../components/features/CreateBird";

export default function Birds() {
  const { user } = useAuth();
  const role = user?.role;

  const [userData, setUserData] = useState<User | null>(null);
  const [search, setSearch] = useState("");
  const [birds, setBirds] = useState<Bird[]>([]);
  const [groups, setGroups] = useState<Group[]>([]);
  const [friends, setFriends] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const BASE_URL = "http://localhost:8080";

  // Fetch full user data
  useEffect(() => {
    if (user?.id) {
      getUserById(user.id)
        .then(setUserData)
        .catch(console.error);
    }
  }, [user?.id]);

  // Fetch user's groups
  useEffect(() => {
    if (user?.id) {
      getUserGroups(user.id)
        .then(setGroups)
        .catch(err => console.error("Failed to fetch groups:", err));
    }
  }, [user?.id]);

  // Fetch friends
  useEffect(() => {
    if (user?.id) {
      fetch(`${BASE_URL}/users/${user.id}/friends`, { credentials: "include" })
        .then(r => r.json())
        .then(setFriends)
        .catch(err => console.error("Failed to fetch friends:", err));
    }
  }, [user?.id]);

  // Fetch birds
  useEffect(() => {
    const fetchBirds = async () => {
      try {
        setLoading(true);
        const data = await getAllBirds();
        setBirds(data);
        setError(null);
      } catch (err) {
        console.error("Error fetching birds:", err);
        setError("Failed to load birds. Please try again later.");
      } finally {
        setLoading(false);
      }
    };

    fetchBirds();
  }, []);

  const filteredBirds = birds.filter(b =>
    b.commonName.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="flex flex-row min-h-screen bg-[#F7F7F7] px-16">
      {/* LEFT SIDEBAR */}
      {(isBasicUser(user?.role) || isAdmin(user?.role) || isSuperUser(user?.role))&&
      <div className="flex flex-col basis-1/4 m-6 mr-0">
        <ProfileCard user={userData || undefined} />
        <div className="h-fit w-full mt-6 bg-white p-4 drop-shadow">
              <div className='flex flex-row w-full border-b border-gray-300 mb-3'>
                <img src="src/assets/groups.svg" alt="groups"/>
                <p className='text-lg ml-3 font-bold'>Groups</p>
              </div>
              {groups.length === 0 ? (
                <p className='text-gray-500 text-sm text-center py-8'>No groups yet</p>
              ) : (
                <>
                  {groups.map((group) => (
                    <GroupCard key={group.id.toString()} group={group}/>
                  ))}
                </>
              )}
              <div className='flex flex-row w-full border-b border-gray-300 mb-3'>
                <img src="src/assets/person.svg" alt="friends"/>
                <p className='text-lg ml-3 font-bold'>Friends</p>
              </div>
              {friends.length === 0 ? (
                <p className='text-gray-500 text-sm text-center py-8'>No friends yet</p>
              ) : (
                <>
                  {friends.map((friend) => (
                    <FriendCard key={friend.id} user={friend}/>
                  ))}
                </>
              )}
        </div>
      </div>}

      {/* CENTER CONTENT */}
      <div className="basis-1/2 m-6">
        <div className="bg-white p-6 rounded-lg shadow-sm">
          <div className="flex items-center justify-between mb-6 gap-4">
            <div className="flex">
              <h2 className="text-2xl font-bold text-gray-800 shrink-0">All Birds</h2>
              
          </div>
            <div className="flex">
              <div className="mr-7 min-w-24">
                {isSuperUser(role) && <CreateBird />}
              </div>
              <SearchBar onChange={(e: any) => setSearch(e.target.value)} />
            </div>
          </div>

          <div className="grid grid-cols-1 gap-2">
            {loading && <p className="text-center py-4">Loading birds...</p>}
            {error && <p className="text-center py-4 text-red-600">{error}</p>}
            {!loading && !error && filteredBirds.length === 0 && (
              <p className="text-center py-4 text-gray-500">No birds found</p>
            )}
            {!loading &&
              !error &&
              filteredBirds.map(bird => (
                <BirdCard key={bird.id} bird={bird} />
              ))}
          </div>
        </div>
      </div>

      {/* RIGHT SIDEBAR */}
      <div className="basis-1/4 m-6 ml-0 h-fit w-full bg-white p-4 drop-shadow">
        <div className="flex flex-row w-full border-b border-gray-300 mb-3 items-center">
          <img src="src/assets/bird.svg" alt="birds" className="w-5 h-5" />
          <div className="text-lg ml-3 font-bold">All Birds</div>
        </div>

        {loading && <p className="text-sm">Loading...</p>}
        {!loading &&
          !error &&
          birds.slice(0, 20).map(bird => (
            <div key={bird.id} className="flex items-center gap-2 mb-2">
              {bird.imageURL && (
                <img
                  src={bird.imageURL}
                  alt={bird.commonName}
                  className="w-8 h-8 rounded-full object-cover"
                />
              )}
              <p className="text-sm">{bird.commonName}</p>
            </div>
          ))}
      </div>
    </div>
  );
}
