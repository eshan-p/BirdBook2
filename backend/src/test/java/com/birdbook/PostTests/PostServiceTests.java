package com.birdbook.PostTests;
import com.birdbook.models.Comment;
import com.birdbook.models.Post;
import com.birdbook.models.PostUser;
import com.birdbook.models.User;
import com.birdbook.repository.PostDAO;
import com.birdbook.repository.UserDAO;
import com.birdbook.service.PostService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTests {
    @Mock
    private PostDAO postDAO;

    @Mock
    UserDAO userDAO;

    @Mock
    private MongoTemplate mongoTemplate;


    @InjectMocks
    private PostService postService;

    private Post testPost;
    private ObjectId postId;
    private ObjectId birdId;
    private ObjectId groupId;
    private ObjectId userId;
    private User testUser;

    @BeforeEach
    void setup(){
        postId = new ObjectId();
        birdId = new ObjectId();
        groupId = new ObjectId();
        userId = new ObjectId();

        testUser = new User();
        ObjectId[] testPostArray = {postId};
        testUser.setPosts(testPostArray);
        testUser.setId(userId);

        PostUser postUser = new PostUser(userId, "testUser");

        //setup test post model
        testPost = new Post();
        testPost.setId(postId);
        testPost.setUser(postUser);
        testPost.setHeader("Staraptor");
        testPost.setTextBody("HE MEGA EVOLVED IN FRONT OF ME");

        testPost.setFlagged(false);
        testPost.setHelp(false);
        testPost.setImage("ImagePathForMegaStaraptor");

        //setting up testing tags
        Map<String, String> tags = new HashMap<>();
        tags.put("location", "Lumiose");
        testPost.setTags(tags);

        testPost.setBird(birdId);
        testPost.setGroup(groupId);

        //might add better data here later
        testPost.setLikes(new ArrayList<>());
        testPost.setComments(new ArrayList<>());
    }

    //get by id
    @Test
    void getPostById_Found_ReturnsPost()  {
        when(postDAO.findById(postId)).thenReturn(Optional.of(testPost));

        Optional<Post> result = postService.getPostById(postId);

        assertTrue(result.isPresent());
        assertEquals("Staraptor", result.get().getHeader());
        verify(postDAO).findById(postId);
    }

    @Test
    void getPostById_NotFound_ReturnsEmpty() {
        when(postDAO.findById(postId)).thenReturn(Optional.empty());

        Optional<Post> result = postService.getPostById(postId);

        assertTrue(result.isEmpty());
        verify(postDAO).findById(postId);
    }

    //update posts
    @Test
    void updatePost_HeaderOnly_DoesNotDeleteComments() {
        Comment comment = new Comment(new PostUser(new ObjectId(), "commenter"), "Nice bird!");
        testPost.setComments(new ArrayList<>(List.of(comment)));

        Post update = new Post();
        update.setHeader("Updated Header");

        when(postDAO.findById(postId)).thenReturn(Optional.of(testPost));
        when(postDAO.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        Post result = postService.updatePost(postId, update, null);

        assertEquals("Updated Header", result.getHeader());
        assertEquals(1, result.getComments().size());
        assertEquals("Nice bird!", result.getComments().get(0).getTextBody());
    }

    @Test
    void updatePost_UpdatesTags() {
        Post update = new Post();

        Map<String, String> newTags = new HashMap<>();
        newTags.put("location", "San Antonio, TX");
        update.setTags(newTags);

        when(postDAO.findById(postId)).thenReturn(Optional.of(testPost));
        when(postDAO.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        Post result = postService.updatePost(postId, update, null);

        assertEquals("San Antonio, TX", result.getTags().get("location"));
    }

    @Test
    void updatePost_PostNotFound_ThrowsException() {
        when(postDAO.findById(postId)).thenReturn(Optional.empty());

        Post update = new Post();

        assertThrows(RuntimeException.class,
                () -> postService.updatePost(postId, update, null));
    }

    //get all posts
    @Test
    void getAllPosts_Success_ReturnsPosts(){
        when(postDAO.findAll()).thenReturn(List.of(testPost));

        List<Post> result = postService.getAllPosts();

        assertEquals(1,result.size());
    }

    @Test
    void getAllPosts_Empty_ReturnsEmpty(){
        when(postDAO.findAll()).thenReturn(new ArrayList<>());

        List<Post> result = postService.getAllPosts();

        assertTrue(result.isEmpty());
    }

    //get all friends posts
    @Test
    void getAllPostsGivenFriendIds_Success_ReturnsPosts(){
        List<ObjectId> friendList = new ArrayList<>();
        friendList.add(testUser.getId());

        ObjectId friendId = new ObjectId();

        testUser.setFriends(new ObjectId[]{ friendId });

        testUser.setFriends(friendList.toArray(new ObjectId[0]));

        User testFriend = new User();
        testFriend.setId(friendId);
        testFriend.setPosts(new ObjectId[]{ postId });

        when(userDAO.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userDAO.findAllById(anyList())).thenReturn(List.of(testFriend));
        when(postDAO.findAllById(anyList())).thenReturn(List.of(testPost));

        //act
        List<Post> result = postService.getAllPostsByFriends(testUser.getId());

        //assert
        assertEquals(1,result.size());
    }

    @Test
    void getAllPostsGivenFriendsIds_Empty_ReturnsEmpty(){
        List<ObjectId> friendList = new ArrayList<>();
        friendList.add(testUser.getId());

        ObjectId friendId = new ObjectId();

        testUser.setFriends(new ObjectId[]{ friendId });

        testUser.setFriends(friendList.toArray(new ObjectId[0]));

        User testFriend = new User();
        testFriend.setId(friendId);
        testFriend.setPosts(new ObjectId[]{ postId });

        when(userDAO.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userDAO.findAllById(anyList())).thenReturn(List.of(testFriend));
        when(postDAO.findAllById(anyList())).thenReturn(new ArrayList<>());

        //act
        List<Post> result = postService.getAllPostsByFriends(testUser.getId());

        //assert
        assertEquals(0,result.size());
    }

    @Test
    void getAllPostsByFriends_NoFriends_ReturnsEmpty() {

        ObjectId userId = new ObjectId();

        User user = new User();
        user.setId(userId);
        user.setFriends(new ObjectId[]{}); // no friends

        when(userDAO.findById(userId))
                .thenReturn(Optional.of(user));

        List<Post> result = postService.getAllPostsByFriends(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllPostsByFriends_UserNotFound_ThrowsException() {

        ObjectId userId = new ObjectId();

        when(userDAO.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> postService.getAllPostsByFriends(userId));
    }


    //create post and return postid
    @Test
    void createPost_Success_ReturnsSavedPost() {
        testUser.setPosts(new ObjectId[]{});

        when(postDAO.save(testPost)).thenReturn(testPost);
        when(userDAO.findById(userId)).thenReturn(Optional.of(testUser));
        when(userDAO.save(any(User.class))).thenReturn(testUser);

        Post result = postService.createPost(testPost, null);

        assertNotNull(result);
        assertEquals(testPost.getHeader(), result.getHeader());
    }

    //delete
    @Test
    void deletePostById_DeletesPost() {

        postService.deletePostById(postId);

        verify(postDAO).deleteById(postId);
    }

    //Retrieve list of posts by tags
    @Test
    void getAllPostsByTags_ReturnsMatchingPosts() {

        Map<String, String> tags = new HashMap<>();
        tags.put("location", "Lumiose");

        when(mongoTemplate.find(any(Query.class), eq(Post.class)))
                .thenReturn(List.of(testPost));

        List<Post> result = postService.getAllPostsByTags(tags);

        assertEquals(1, result.size());
        assertEquals("Staraptor", result.get(0).getHeader());
    }
}