import ProfileIcon from "../common/ProfileIcon";
import { User } from "../../types/User";
import { Friend } from "../../types/Friend";
import { resolveMediaUrl } from "../../utils/mediaUrl";

type FriendCardProps =
  | { user: User; friend?: never }
  | { friend: Friend; user?: never };

function FriendCard(props: FriendCardProps) {
  let name: string;
  let profilePhoto: string | undefined;
  let subText: string;

  if ("user" in props && props.user) {
    const user = props.user;

    name = user.username;
    profilePhoto = user.profilePic ? resolveMediaUrl(user.profilePic) : undefined;
    subText = `${user.friends?.length || 0} friends`;
  } else {
    const friend = props.friend;

    name = friend.name;
    profilePhoto = friend.profilePhoto;
    subText = friend.location
      ? friend.location.join(", ")
      : "Unknown location";
  }

  return (
    <div className="flex items-center gap-4 px-2 py-2 hover:bg-gray-100 rounded cursor-pointer">
      <ProfileIcon size="sm" src={profilePhoto} />

      <div className="flex flex-col justify-center">
        <h3 className="text-sm font-semibold text-gray-900 leading-tight">
          {name}
        </h3>

        <p className="text-xs text-gray-500">
          {subText}
        </p>
      </div>
    </div>
  );
}

export default FriendCard;
