package com.example.post.service;


import com.example.post.models.Comment;
import com.example.post.models.Post;
import com.example.post.models.User;
import com.example.post.repository.PostDAO;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostDAO sDAO;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String postPrefix;

    public PostService(
            PostDAO sDAO,
            UserService userService,
            MongoTemplate mongoTemplate,
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucket:}") String bucketName,
            @Value("${aws.s3.post-prefix:images}") String postPrefix
    ) {
        this.sDAO = sDAO;
        this.userService = userService;
        this.mongoTemplate = mongoTemplate;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.postPrefix = postPrefix;
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

        return withResolvedPostImages(results.getMappedResults());
    }

    // Just for testing Spring Boot, can be removed later
    public Optional<Post> getPostById(ObjectId id) {
        Optional<Post> post = sDAO.findById(id);
        if (post.isPresent()) {
            List<Post> enriched = postsWithBirdLookup(List.of(post.get()));
            return enriched.isEmpty() ? post.map(this::withResolvedPostImage) : Optional.of(enriched.get(0));
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
        Optional<Post> existingPost = sDAO.findById(id);
        existingPost.ifPresent(post -> deletePostImageIfManaged(post.getImage()));
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
            deletePostImageIfManaged(existingPost.getImage());
            String imagePath = uploadImageToS3(imageFile);
            existingPost.setImage(imagePath);
        }

        Post saved = sDAO.save(existingPost);
        List<Post> enriched = postsWithBirdLookup(List.of(saved));
        return enriched.isEmpty() ? saved : enriched.get(0);
    }

    public Post createPost(Post newPost, MultipartFile imageFile) {
        // Handle image file if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = uploadImageToS3(imageFile);
            newPost.setImage(imagePath);
        }

        Post savedPost = sDAO.save(newPost);

        String userId = savedPost.getUser().getUserId();   // should be String
        String postId = savedPost.getId().toHexString();

        userService.addPostToUser(userId, postId);

        List<Post> enriched = postsWithBirdLookup(List.of(savedPost));
        return enriched.isEmpty() ? savedPost : enriched.get(0);
    }

    private String uploadImageToS3(MultipartFile imageFile){
        try {
            requireS3Configured();

            String cleanPrefix = postPrefix == null ? "images" : postPrefix.trim();
            if (cleanPrefix.isEmpty()) {
                cleanPrefix = "images";
            }

            String originalFileName = imageFile.getOriginalFilename() == null
                    ? "image"
                    : imageFile.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
            String objectKey = cleanPrefix + "/" + UUID.randomUUID() + "_" + originalFileName;
            String contentType = imageFile.getContentType() == null
                    ? "application/octet-stream"
                    : imageFile.getContentType();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(imageFile.getInputStream(), imageFile.getSize())
            );

            return objectKey;

        } catch (IOException e){
            throw new RuntimeException("Failed to store image", e);
        }
    }

    private List<Post> withResolvedPostImages(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return posts;
        }
        return posts.stream().map(this::withResolvedPostImage).toList();
    }

    private Post withResolvedPostImage(Post post) {
        if (post == null || post.getImage() == null || post.getImage().isBlank()) {
            return post;
        }

        String imageReference = post.getImage();
        if (imageReference.startsWith("http://") || imageReference.startsWith("https://") || imageReference.startsWith("/")) {
            return post;
        }

        try {
            requireS3Configured();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageReference)
                    .build();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .getObjectRequest(getObjectRequest)
                    .build();

            String presignedUrl = s3Presigner.presignGetObject(getObjectPresignRequest)
                    .url()
                    .toExternalForm();
            post.setImage(presignedUrl);
        } catch (Exception ignored) {
        }

        return post;
    }

    private void deletePostImageIfManaged(String imageReference) {
        if (imageReference == null || imageReference.isBlank()) {
            return;
        }
        if (imageReference.startsWith("http://") || imageReference.startsWith("https://") || imageReference.startsWith("/")) {
            return;
        }

        try {
            requireS3Configured();

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageReference)
                    .build());
        } catch (Exception ignored) {
        }
    }

    private void requireS3Configured() {
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException("S3 bucket is not configured. Set AWS_S3_BUCKET.");
        }
    }

    public List<Post> getAllPostsByFriends(String userId) {
        User user = userService.getUserById(userId);

        ObjectId[] friendIds = user.getFriends();

        if (friendIds == null || friendIds.length == 0){
            return List.of();
        }

        List<User> friends = userService.findAllById(List.of(friendIds));

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
        
        List<User> likers = userService.findAllById(likerIds);
        
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