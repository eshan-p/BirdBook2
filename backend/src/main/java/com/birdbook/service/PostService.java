package com.birdbook.service;

import com.birdbook.models.Comment;
import com.birdbook.models.Post;
import com.birdbook.models.User;
import com.birdbook.repository.PostDAO;
import com.birdbook.repository.UserDAO;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostDAO sDAO;
    private final UserDAO userDAO;
    private final MongoTemplate mongoTemplate;

    public PostService(PostDAO sDAO, UserDAO userDAO, MongoTemplate mongoTemplate) {
        this.sDAO = sDAO;
        this.userDAO = userDAO;
        this.mongoTemplate = mongoTemplate;
    }

    private List<Post> postsWithBirdLookup(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return posts;
        }

        List<AggregationOperation> pipeline = Arrays.asList(
            Aggregation.match(Criteria.where("_id").in(
                posts.stream().map(Post::getId).collect(Collectors.toList())
            )),
            Aggregation.lookup("birds", "bird", "_id", "birdDetails"),
            Aggregation.unwind("$birdDetails", true) // preserveNullAndEmptyArrays = true
        );

        AggregationResults<Post> results = mongoTemplate.aggregate(
            Aggregation.newAggregation(pipeline),
            "posts",
            Post.class
        );

        return results.getMappedResults();
    }

    // Just for testing Spring Boot, can be removed later
    public Optional<Post> getPostById(ObjectId id) {
        Optional<Post> post = sDAO.findById(id);
        if (post.isPresent()) {
            List<Post> enriched = postsWithBirdLookup(List.of(post.get()));
            return enriched.isEmpty() ? post : Optional.of(enriched.get(0));
        }
        return post;
    }

    public List<Post> getAllPosts() {
        return postsWithBirdLookup(sDAO.findAll());
    }

    public List<Post> getAllPostsByGroup(ObjectId groupId) {
        return postsWithBirdLookup(sDAO.findByGroup(groupId));
    }

    public void deletePostById(ObjectId id){
        sDAO.deleteById(id);
    }

    public Post updatePost(ObjectId id, Post updatedPost, MultipartFile imageFile) {
        Post existingPost = sDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Update only fields that are not null
        if (updatedPost.getHeader() != null) {
            existingPost.setHeader(updatedPost.getHeader());
        }
        if (updatedPost.getTextBody() != null) {
            existingPost.setTextBody(updatedPost.getTextBody());
        }
        if (updatedPost.getComments() != null && !updatedPost.getComments().isEmpty()) {
            existingPost.setComments(updatedPost.getComments());
        }
        if (updatedPost.getLikes() != null) {
            existingPost.setLikes(updatedPost.getLikes());
        }
        if (updatedPost.getTags() != null) {
            existingPost.setTags(updatedPost.getTags());
        }
        if (updatedPost.getFlagged() != null) {
            existingPost.setFlagged(updatedPost.getFlagged());
        }
        if (updatedPost.getHelp() != null) {
            existingPost.setHelp(updatedPost.getHelp());
        }

        // Handle image file if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            existingPost.setImage(imagePath);
        }

        Post saved = sDAO.save(existingPost);
        List<Post> enriched = postsWithBirdLookup(List.of(saved));
        return enriched.isEmpty() ? saved : enriched.get(0);
    }

    public Post createPost(Post newPost, MultipartFile imageFile) {
        // Handle image file if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            newPost.setImage(imagePath);
        }

        Post savedPost = sDAO.save(newPost);

        ObjectId userId = savedPost.getUser().getUserId();

        User user = userDAO.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        ObjectId[] currentPosts = user.getPosts();
        ObjectId[] updatedPosts = Arrays.copyOf(currentPosts, currentPosts.length + 1);

        updatedPosts[currentPosts.length] = savedPost.getId();
        user.setPosts(updatedPosts);

        userDAO.save(user);

        List<Post> enriched = postsWithBirdLookup(List.of(savedPost));
        return enriched.isEmpty() ? savedPost : enriched.get(0);
    }

    // Helper method to save image file for adding/updating a post; returns the file path
    private String saveImage(MultipartFile imageFile){
        try {
            String uploadDir = "images";
            Files.createDirectories(Paths.get(uploadDir));

            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            Files.copy(imageFile.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            return "/" + uploadDir + "/" + fileName;

        } catch (IOException e){
            throw new RuntimeException("Failed to store image", e);
        }
    }

    public List<Post> getAllPostsByFriends(ObjectId userId) {
        User user = userDAO.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));

        ObjectId[] friendIds = user.getFriends();

        if (friendIds == null || friendIds.length == 0){
            return List.of();
        }

        List<User> friends = userDAO.findAllById(List.of(friendIds));

        List<ObjectId> allPostIds = new ArrayList<>();

        for (User friend: friends){
            if(friend.getPosts() != null){
                allPostIds.addAll(List.of(friend.getPosts()));
            }
        }

        if (allPostIds.isEmpty()){
            return List.of();
        }

        return postsWithBirdLookup(sDAO.findAllById(allPostIds));
    }

    public List<Post> getAllPostsByTags(Map<String,String> tags) {
        Query query = new Query();

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            query.addCriteria(Criteria.where("tags." + key).is(value));
        }

        return postsWithBirdLookup(mongoTemplate.find(query, Post.class));
    }

    public Post addComment(ObjectId postId, Comment comment) {
        Post post = sDAO.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        post.getComments().add(comment);
        Post saved = sDAO.save(post);
        List<Post> enriched = postsWithBirdLookup(List.of(saved));
        return enriched.isEmpty() ? saved : enriched.get(0);
    }

    public Post updateComment(ObjectId postId, ObjectId userId, Comment updatedComment) {

        Post post = sDAO.findById(postId).orElseThrow(() -> new IllegalArgumentException("Post not found"));

        boolean updated = false;

        for (Comment c : post.getComments()) {
            if (c.getUser().getUserId().equals(userId) && c.getTimestamp().equals(updatedComment.getTimestamp())) {
                c.setTextBody(updatedComment.getTextBody());
                updated = true;
                break;
            }
        }

        if (!updated) {
            throw new IllegalArgumentException("Comment not found or unauthorized");
        }

        Post saved = sDAO.save(post);
        List<Post> enriched = postsWithBirdLookup(List.of(saved));
        return enriched.isEmpty() ? saved : enriched.get(0);
    }

    public Post deleteComment(ObjectId postId, ObjectId userId, Date timestamp) {

        Post post = sDAO.findById(postId).orElseThrow(() -> new IllegalArgumentException("Post not found"));

        boolean removed = post.getComments().removeIf(
            c -> c.getUser().getUserId().equals(userId)
            && c.getTimestamp().equals(timestamp)
        );

        if (!removed) {
            throw new IllegalArgumentException("Comment not found or unauthorized");
        }

        Post saved = sDAO.save(post);
        List<Post> enriched = postsWithBirdLookup(List.of(saved));
        return enriched.isEmpty() ? saved : enriched.get(0);
    }

    public Post likePost(ObjectId postId, ObjectId userId) {
        Post post = sDAO.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        if (!post.getLikes().contains(userId)) {
            post.getLikes().add(userId);
        }
        
        Post saved = sDAO.save(post);
        List<Post> enriched = postsWithBirdLookup(List.of(saved));
        return enriched.isEmpty() ? saved : enriched.get(0);
    }

    public Post unlikePost(ObjectId postId, ObjectId userId) {

        Post post = sDAO.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        post.getLikes().remove(userId);
        Post saved = sDAO.save(post);
        List<Post> enriched = postsWithBirdLookup(List.of(saved));
        return enriched.isEmpty() ? saved : enriched.get(0);
    }

    public Post flagPost(ObjectId postId) {

        Post post = sDAO.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        post.setFlagged(true);
        Post saved = sDAO.save(post);
        List<Post> enriched = postsWithBirdLookup(List.of(saved));
        return enriched.isEmpty() ? saved : enriched.get(0);
    }

    public Post unflagPost(ObjectId postId) {

        Post post = sDAO.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        post.setFlagged(false);
        Post saved = sDAO.save(post);
        List<Post> enriched = postsWithBirdLookup(List.of(saved));
        return enriched.isEmpty() ? saved : enriched.get(0);
    }

    public Post markNeedsHelp(ObjectId postId) {

        Post post = sDAO.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        post.setHelp(true);
        Post saved = sDAO.save(post);
        List<Post> enriched = postsWithBirdLookup(List.of(saved));
        return enriched.isEmpty() ? saved : enriched.get(0);
    }

    public Post removeHelpFlag(ObjectId postId) {

        Post post = sDAO.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        post.setHelp(false);
        Post saved = sDAO.save(post);
        List<Post> enriched = postsWithBirdLookup(List.of(saved));
        return enriched.isEmpty() ? saved : enriched.get(0);
    }

    public List<Map<String, String>> getUsersWhoLiked(ObjectId postId) {
        
        Post post = sDAO.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        List<ObjectId> likerIds = post.getLikes();
        
        if (likerIds == null || likerIds.isEmpty()) {
            return List.of();
        }
        
        List<User> likers = userDAO.findAllById(likerIds);
        
        return likers.stream()
            .map(user -> Map.of(
                "id", user.getId().toString(),
                "username", user.getUsername()
            ))
            .collect(Collectors.toList());
    }

    public boolean isOwner(ObjectId postId, String userId) {
        Optional<Post> post = sDAO.findById(postId);
        return post.isPresent() && post.get().getUser().getUserId().equals(new ObjectId(userId));
    }
}