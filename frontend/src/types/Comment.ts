export interface Comment {
  //id: string;
  user:{
    id: string,
    username: string,
    profilePic?: string
  };
  textBody:string;
  timestamp:string;//iso date string
}
