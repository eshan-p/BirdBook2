import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Group } from "../types/Group";
import {
  getAllGroups,
  getUserGroups,
  requestToJoinGroup,
  leaveGroup,
  createGroup,
  deleteGroup,
} from "../api/Groups";
import SearchBar from "../components/common/SearchBar";
import GroupCard from "../components/features/GroupCard";
import { useAuth } from "../context/AuthContext";
import CreateGroup from "../components/features/CreateGroup";
import ProfileCard from "../components/features/ProfileCard";
import FriendCard from "../components/features/FriendCard";
import { User } from "../types/User";
import { Bird } from "../types/Bird";
import { getAllBirds } from "../api/Birds";
import { getUserById } from "../api/Users";
import GroupFormCard from "../components/common/GroupFormCard";

export default function Groups() {
  const [allGroups, setAllGroups] = useState<Group[]>([]);
  const [userGroups, setUserGroups] = useState<Group[]>([]);
  const [pageLoading, setPageLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [newGroupName, setNewGroupName] = useState("");
  const { user, loading } = useAuth();
  const navigate = useNavigate();
  const [userData, setUserData] = useState<User | null>(null);
  const [friends, setFriends] = useState<User[]>([]);
  const [birds, setBirds] = useState<Bird[]>([]);
  const [search, setSearch] = useState("");
  const BASE_URL = "http://localhost:8080";

  const currentUserId = user?.id || "";
  const role = user?.role;
  const canManage = role === "ADMIN_USER" || role === "SUPER_USER";

  useEffect(() => {
    async function fetchGroups() {
      try {
        const data = await getAllGroups();
        setAllGroups(data);

        if (currentUserId) {
          const userGroupsData = await getUserGroups(currentUserId);
          setUserGroups(userGroupsData);
        }
      } catch (err: any) {
        setError(err.message || "Failed to fetch groups.");
      } finally {
        setPageLoading(false);
      }
    }
    fetchGroups();
  }, [currentUserId]);

  useEffect(() => {
    if (!user && !loading) {
      navigate("/login");
    }
  }, [user, loading, navigate]);

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
    getAllBirds()
        .then(setBirds)
        .catch(err => console.error("Failed to fetch birds:", err));
  }, []); // get birds

  const refreshLists = async () => {
    const data = await getAllGroups();
    setAllGroups(data);
    if (currentUserId) {
      const userGroupsData = await getUserGroups(currentUserId);
      setUserGroups(userGroupsData);
    }
  };

  const handleJoin = async (groupId: string) => {
    if (!currentUserId) {
      alert("Please log in to join a group");
      return;
    }
    try {
      await requestToJoinGroup(groupId, currentUserId);
      await refreshLists();
    } catch (err: any) {
      console.error("Failed to join group:", err);
      const errorMessage = err.message || 'Failed to send join request';
      if (!errorMessage.includes('already')) {
        alert(errorMessage);
      }
    }
  };

  const handleLeave = async (groupId: string) => {
    if (!currentUserId) {
      alert("Please log in to leave a group");
      return;
    }
    try {
      await leaveGroup(groupId, currentUserId);
      const group = userGroups.find((g) => g.id === groupId);
      if (group) {
        setUserGroups(userGroups.filter((g) => g.id !== groupId));
        setAllGroups([...allGroups, group]);
      }
    } catch (err: any) {
      console.error("Failed to leave group:", err);
    }
  };

  const handleCreate = async () => {
    if (!currentUserId || !newGroupName.trim()) return;
    try {
      await createGroup(newGroupName.trim(), currentUserId);
      setNewGroupName("");
      await refreshLists();
    } catch (err: any) {
      console.error("Failed to create group:", err);
    }
  };

  const handleDelete = async (groupId: string) => {
    if (!canManage || !currentUserId) return;
    try {
      await deleteGroup(groupId, currentUserId);
      setAllGroups(allGroups.filter((g) => g.id !== groupId));
      setUserGroups(userGroups.filter((g) => g.id !== groupId));
    } catch (err: any) {
      console.error("Failed to delete group:", err);
    }
  };

  if (pageLoading) return <p>Loading...</p>;

  const filteredAllGroups = allGroups.filter(group =>
    group.name.toLowerCase().includes(search.toLowerCase())
  );

  const filteredUserGroups = userGroups.filter(group =>
    group.name.toLowerCase().includes(search.toLowerCase())
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
          {userGroups.length === 0 ? (
            <p className='text-gray-500 text-sm text-center py-8'>No groups yet</p>
          ) : (
            <>
              {userGroups.map((group) => (
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
            <div className="flex">
              <h2 className="text-2xl font-bold text-gray-800 shrink-0">Groups</h2>
              
          </div>
            <div className="flex">
              <div className="mr-7 min-w-24">
                {canManage && <CreateGroup/>}
              </div>
              <SearchBar onChange={(e: any) => setSearch(e.target.value)} />
            </div>
          </div>

          {/* {canManage && <CreateGroup/>} */}

          {error && (
            <div className="mb-4 p-4 bg-red-100 text-red-700 rounded">Error: {error}</div>
          )}

          {currentUserId && filteredUserGroups.length > 0 && (
            <div className="mb-8">
              <h3 className="text-lg font-semibold text-gray-800 mb-3">My Groups</h3>
              <ul className="space-y-2">
                {filteredUserGroups.map((group) => (
                  <li key={group.id}>
                    <GroupCard
                      group={group}
                    />
                  </li>
                ))}
              </ul>
            </div>
          )}

          <div>
            <h3 className="text-lg font-semibold text-gray-800 mb-3">Available Groups</h3>
            <ul className="space-y-2">
              {filteredAllGroups.map((group) => (
                <li key={group.id}>
                  <GroupCard
                    group={group}
                  />
                </li>
              ))}
            </ul>
            {filteredAllGroups.length === 0 && (
              <p className="text-gray-500 py-4">No available groups</p>
            )}
          </div>
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