export interface PostUser {
    userId: string;
    username: string;
    profilePic?: string;
}

export interface Group {
    id: string;
    name: string;
    description?: string;
    owner: PostUser;
    members: PostUser[];
    requests: PostUser[];
    groupPhoto?: string;
    location?: [number, number];
    followers?: number;
}