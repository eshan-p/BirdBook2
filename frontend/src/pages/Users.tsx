import { useEffect, useState } from "react";
import { User } from "../types/User";
import { getAllUsers, getUserById, updateUserRole } from "../api/Users";
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


export default function Users() {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [friends, setFriends] = useState<User[]>([]);
    const [userGroups, setUserGroups] = useState<Group[]>([]);
    const navigate = useNavigate();
    const { user, loading: authLoading } = useAuth();
    const [userData, setUserData] = useState<User | null>(null);
    const [groups, setGroups] = useState<Group[]>([]);
    const [birds, setBirds] = useState<Bird[]>([]);
    const [search, setSearch] = useState("");
    const BASE_URL = "http://localhost:8080";

    useEffect(() => {
        async function fetchUsers() {
            try {
                const data = await getAllUsers();
                setUsers(data);
            } catch (err: any) {
                setError(err.message || "Failed to fetch users.");
            } finally {
                setLoading(false);
            }
        }

        fetchUsers();
    }, []);

    useEffect(() => {
        if(user?.id) {
            getUserById(user.id)
              .then(setUserData)
              .catch(console.error);
        }
    }, [user?.id]);

    useEffect(() => {
    if(user?.id) {
        getUserById(user.id)
          .then(setUserData)
          .catch(console.error);
        
        fetch(`${BASE_URL}/users/${user.id}/friends`, {credentials: 'include'})
          .then(r => r.json())
          .then(setFriends)
          .catch(err => console.error("Failed to fetch friends:", err));
    }
    }, [user?.id]);

    useEffect(() => {
        if(user?.id) {
            getUserGroups(user.id)
                .then(setUserGroups)
                .catch(err => console.error("Failed to fetch groups:", err));
        }
    }, [user?.id]);

    useEffect(() => {
        getAllBirds()
            .then(setBirds)
            .catch(err => console.error("Failed to fetch birds:", err));
    }, []);

    useEffect(() => {
        if(!user && !loading){
          navigate('/login')
          console.error("Not signed in!")
        }
      }, [user, loading, navigate]);

    if (authLoading || loading) return <p>Loading...</p>;
    if (error) return <p>Error: {error}</p>;

    const filteredUsers = users.filter(u =>
        u.username.toLowerCase().includes(search.toLowerCase())
    );

    const handleRoleChange = async (userId: string, newRole: string) => {
        try {
            await updateUserRole(userId, newRole);
            // Update the local state to reflect the change
            setUsers(users.map(u => 
                u.id === userId ? { ...u, role: newRole } : u
            ));
        } catch (err: any) {
            console.error("Failed to update user role:", err);
            alert(`Failed to update user role: ${err.message}. Check console for details.`);
        }
    };

    const isSuperUser = userData?.role === 'SUPER_USER';

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
                            All Users
                        </h2>

                        <div className="w-64">
                            <SearchBar onChange={(e: any) => setSearch(e.target.value)} />
                        </div>
                    </div>

                    <ul className="space-y-2">
                        {filteredUsers.map(user => (
                            <li 
                                key={user.id}
                                className="rounded-lg p-2"
                            >
                                <div className="flex items-center justify-between">
                                    <div 
                                        onClick={() => navigate(`/user/${user.id}`)}
                                        className="flex-1 cursor-pointer"
                                    >
                                        <FriendCard user={user} />
                                    </div>
                                    
                                    {isSuperUser && (
                                        <div className="ml-4 flex items-center gap-2">
                                            <label htmlFor={`role-${user.id}`} className="text-sm font-medium text-gray-700">
                                                Role:
                                            </label>
                                            <select
                                                id={`role-${user.id}`}
                                                value={user.role}
                                                onChange={(e) => handleRoleChange(user.id, e.target.value)}
                                                className="px-3 py-1.5 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                            >
                                                <option value="BASIC_USER">Basic</option>
                                                <option value="ADMIN_USER">Admin</option>
                                                <option value="SUPER_USER">Super</option>
                                            </select>
                                        </div>
                                    )}
                                </div>
                            </li>
                        ))}
                    </ul>
                    {filteredUsers.length === 0 && (
                        <p className="text-gray-500 py-4">No users found</p>
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
