import { useEffect, useState } from "react";
import { User } from "../types/User";
import { getFriends, getUserById } from "../api/Users";
import SearchBar from "../components/common/SearchBar";
import FriendCard from "../components/features/FriendCard";
import { useNavigate } from 'react-router-dom';
import ProfileCard from "../components/features/ProfileCard";
import GroupCard from "../components/features/GroupCard";
import { Group } from "../types/Group";
import { Bird } from "../types/Bird";
import { getAllBirds } from "../api/Birds";
import { useAuth } from "../context/AuthContext";
import { getUserGroups } from "../api/Groups";


export default function Friends() {
    const [friends, setFriends] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();
    const { user } = useAuth();
    const [userData, setUserData] = useState<User | null>(null);
    const [groups, setGroups] = useState<Group[]>([]);
    const [birds, setBirds] = useState<Bird[]>([]);
    const [search, setSearch] = useState("");
    const BASE_URL = "http://localhost:8080";

    useEffect(() => {
        async function fetchFriends() {
            if (!user?.id) {
                setLoading(false);
                return;
            }
            
            try {
                const data = await getFriends(user.id); // fetch user's friends only
                setFriends(data);
            } catch (err: any) {
                setError(err.message || "Failed to fetch friends.");
            } finally {
                setLoading(false);
            }
        }

        fetchFriends();
    }, [user?.id]);

    useEffect(() => {
        if(user?.id) {
            getUserById(user.id)
              .then(setUserData)
              .catch(console.error);
        }
    }, [user?.id]);

    useEffect(() => {
        if(user?.id) {
            getUserGroups(user.id)
                .then(setGroups)
                .catch(err => console.error("Failed to fetch groups:", err));
        }
    }, [user?.id]); // get user's groups only

    useEffect(() => {
        getAllBirds()
            .then(setBirds)
            .catch(err => console.error("Failed to fetch birds:", err));
    }, []); // get birds

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error: {error}</p>;

    const filteredFriends = friends.filter(friend =>
        friend.username.toLowerCase().includes(search.toLowerCase())
    );

    return (
        <div className='flex flex-row h-full bg-[#F7F7F7] px-16'>
            {/* Left Sidebar */}
            <div className='flex flex-col basis-1/4 m-6 mr-0'>
                <ProfileCard user={userData || undefined}/>
                <div className='h-fit w-full mt-6 bg-white p-4 drop-shadow'>
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
            </div>

            {/* Main Content */}
            <div className='basis-1/2 m-6'>
                <div className='bg-white p-6 rounded-lg shadow-sm'>
                    <div className="flex items-center justify-between mb-6 gap-4">
                        <h2 className="text-2xl font-bold text-gray-800 shrink-0">
                            Friends
                        </h2>

                        <div className="w-64">
                            <SearchBar onChange={(e: any) => setSearch(e.target.value)} />
                        </div>
                    </div>

                    <ul className="space-y-2">
                        {filteredFriends.map(friend => (
                            <li 
                                key={friend.id}
                                onClick={() => navigate(`/user/${friend.id}`)}
                            >
                                <FriendCard user={friend} />
                            </li>
                        ))}
                    </ul>
                    {filteredFriends.length === 0 && (
                        <p className="text-gray-500 py-4">No friends found</p>
                    )}
                </div>
            </div>

            {/* Right Sidebar */}
            <div className='basis-1/4 m-6 ml-0 h-fit w-full bg-white p-4 drop-shadow'>
                <div className='flex flex-row w-full border-b border-gray-300 mb-3 items-center'>
                    <img src="src/assets/bird.svg" alt="birds" className='w-5 h-5'/>
                    <div className='text-lg ml-3 font-bold'>All Birds</div>
                </div>
                {birds.length === 0 ? (
                    <p className='text-sm'>Loading...</p>
                ) : (
                    birds.slice(0, 20).map(bird => (
                        <div key={bird.id} className='flex items-center gap-2 mb-2'>
                            {bird.imageURL && (
                                <img 
                                    src={bird.imageURL} 
                                    alt={bird.commonName}
                                    className='w-8 h-8 rounded-full object-cover'
                                />
                            )}
                            <p className='text-sm'>
                                {bird.commonName}
                            </p>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}
