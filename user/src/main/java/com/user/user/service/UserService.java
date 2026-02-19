package com.user.user.service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.*;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import com.user.user.models.Bird;
import com.user.user.models.Group;
import com.user.user.models.Post;
import com.user.user.models.Role;
import com.user.user.models.User;
import com.user.user.repository.GroupDAO;
import com.user.user.repository.PostDAO;
import com.user.user.repository.UserDAO;
import com.user.user.repository.BirdDAO;

@Service
public class UserService {

    private final GroupDAO groupDAO;
    private final UserDAO userDAO;
    private final PostDAO postDAO;
    private final BirdDAO birdDAO;
    private final PasswordEncoder passwordEncoder;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String profilePrefix;

    public UserService(
            UserDAO userDAO,
            PostDAO postDAO,
            BirdDAO birdDAO,
            PasswordEncoder passwordEncoder,
            GroupDAO groupDAO,
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucket:}") String bucketName,
            @Value("${aws.s3.profile-prefix:profile_pictures}") String profilePrefix
    ) {
        this.groupDAO = groupDAO;
        this.userDAO = userDAO;
        this.postDAO = postDAO;
        this.birdDAO = birdDAO;
        this.passwordEncoder = passwordEncoder;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.profilePrefix = profilePrefix;
    }

    public void completeOnboarding(String userId, String firstName, String lastName, String location, MultipartFile profilePhoto) {
        try{
            ObjectId objectId = new ObjectId(userId);
            User user = userDAO.findById(objectId).orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setLocation(location);
            user.setOnboardingComplete(true);
            if(profilePhoto != null && !profilePhoto.isEmpty()){
                deleteProfileImageIfManaged(user.getProfilePic());
                String imageKey = uploadProfileImageToS3(profilePhoto);
                user.setProfilePic(imageKey);
            }
            userDAO.save(user);
        } catch(IOException e) {
            throw new RuntimeException("Failed to save profile photo",e);
        }
    }

    public List<User> getAllUsers() {
        return userDAO.findAll().stream().map(this::withResolvedProfilePic).toList();
    }

    public Optional<User> getByUsername(String username) {
        return userDAO.findByUsername(username).map(this::withResolvedProfilePic);
    }

    public User getUserById(ObjectId id){
        User user = userDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return withResolvedProfilePic(user);
    }

    public List<User> searchUsersByUsername(String query) {
        List<User> allUsers = userDAO.findAll();
        
        return allUsers.stream()
            .filter(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()))
            .map(this::withResolvedProfilePic)
            .collect(Collectors.toList());
    }

    public List<Group> getGroupsList(ObjectId userId) {
        User user = userDAO.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        ObjectId[] groupIds = user.getGroups();

        return groupDAO.findAllById(List.of(groupIds));
    } 
    
    public List<Post> getPostsList(ObjectId userId) {
        User user = userDAO.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        ObjectId[] postIds = user.getPosts();
        return postDAO.findAllById(List.of(postIds));
    }

public List<Map<String, Object>> getTopBirdsThisMonth(ObjectId userId) {
    List<Post> userPosts = postDAO.findByUserId(userId);
    LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
    
    // Filter posts from this month that have a bird and a valid timestamp
    Map<ObjectId, Long> birdCounts = userPosts.stream()
        .filter(post -> 
            post.getTimestamp() != null && 
            post.getBird() != null && // Only posts with birds
            post.getTimestamp().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
                .isAfter(startOfMonth)
        )
        .collect(Collectors.groupingBy(Post::getBird, Collectors.counting()));

    System.out.println("Bird counts for user " + userId + ": " + birdCounts);

    // If no birds found, return empty list
    if (birdCounts.isEmpty()) {
        return List.of();
    }

    // Look up bird details for the top 5 birds
    List<Map<String, Object>> topBirds = birdCounts.entrySet().stream()
        .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
        .limit(5)
        .map(entry -> {
            try {
                Bird bird = birdDAO.findById(entry.getKey()).orElse(null);
                if (bird != null) {
                    Map<String, Object> birdMap = new HashMap<>();
                    birdMap.put("id", bird.getId());
                    birdMap.put("commonName", bird.getCommonName());
                    birdMap.put("scientificName", bird.getScientificName() != null ? bird.getScientificName() : "");
                    birdMap.put("imageURL", bird.getImageURL() != null ? bird.getImageURL() : "");
                    birdMap.put("count", (Object) entry.getValue());
                    return birdMap;
                }
            } catch (Exception e) {
                System.err.println("Error fetching bird " + entry.getKey() + ": " + e.getMessage());
            }
            return null;
        })
        .filter(map -> map != null)
        .collect(Collectors.toList());
        System.out.println("Top birds (with details): " + topBirds);
        return topBirds;
    }

    public User registerUser(String username, String password){

        if (userDAO.findByUsername(username).isPresent()){
            throw new IllegalArgumentException("Username already taken.");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(username, hashedPassword);

        userDAO.insert(newUser);

        return newUser;
    }

    /* public User updateUser(ObjectId id, User updatedData){

        User existingUser = userDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));

        existingUser.setUsername(updatedData.getUsername());
        existingUser.setPassword(updatedData.getPassword());

        return userDAO.save(existingUser);
    } */

    public User updateUser(ObjectId id, User updatedUser, MultipartFile imageFile){
        User existingUser = userDAO.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        existingUser.setUsername(updatedUser.getUsername());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            deleteProfileImageIfManaged(existingUser.getProfilePic());
            try {
                String imageKey = uploadProfileImageToS3(imageFile);
                existingUser.setProfilePic(imageKey);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store profile image", e);
            }
        }

        return withResolvedProfilePic(userDAO.save(existingUser));
    }

    public void deleteUser(ObjectId id){
        User existingUser = userDAO.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        deleteProfileImageIfManaged(existingUser.getProfilePic());
        userDAO.deleteById(id);
    }

    public User updateUserRole(ObjectId userId, String newRoleString) {
        User user = userDAO.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
        
        Role newRole;
        
        try {
            newRole = Role.valueOf(newRoleString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + newRoleString);
        }
        
        user.setRole(newRole);
        return userDAO.save(user);
    }

    public void addFriend(ObjectId userId, ObjectId friendId) {
        User user = userDAO.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        userDAO.findById(friendId).orElseThrow(() -> new IllegalArgumentException("Friend not found."));

        ObjectId[] currentFriends = user.getFriends();
        ObjectId[] updatedFriends = new ObjectId[currentFriends.length + 1];
        System.arraycopy(currentFriends, 0, updatedFriends, 0, currentFriends.length);
        updatedFriends[currentFriends.length] = friendId;

        user.setFriends(updatedFriends);
        userDAO.save(user);
    }

    public void addGroup(ObjectId userId, ObjectId groupId) {
        User user = userDAO.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        ObjectId[] currentGroups = user.getGroups();
        ObjectId[] updatedGroups = new ObjectId[currentGroups.length + 1];
        System.arraycopy(currentGroups, 0, updatedGroups, 0, currentGroups.length);
        updatedGroups[currentGroups.length] = groupId;
        user.setGroups(updatedGroups);
        userDAO.save(user);
    }

    public void addPost(ObjectId userId, ObjectId postId) {
        User user = userDAO.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        ObjectId[] currentPosts = user.getPosts();
        ObjectId[] updatedPosts = new ObjectId[currentPosts.length + 1];
        System.arraycopy(currentPosts, 0, updatedPosts, 0, currentPosts.length);
        updatedPosts[currentPosts.length] = postId;
        user.setPosts(updatedPosts);
        userDAO.save(user);
    }

    public void removeFriend(ObjectId userId, ObjectId friendId) {
        User user = userDAO.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        ObjectId[] currentFriends = user.getFriends();
        
        ObjectId[] updatedFriends = java.util.Arrays.stream(currentFriends)
            .filter(id -> !id.equals(friendId))
            .toArray(ObjectId[]::new);
        
        user.setFriends(updatedFriends);
        userDAO.save(user);
    }

    public List<User> getFriendsList(ObjectId userId) {
        User user = userDAO.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        ObjectId[] friendIds = user.getFriends();

        return userDAO.findAllById(List.of(friendIds)).stream().map(this::withResolvedProfilePic).toList();
    } 

    private String uploadProfileImageToS3(MultipartFile imageFile) throws IOException {
        requireS3Configured();

        String cleanPrefix = profilePrefix == null ? "profile_pictures" : profilePrefix.trim();
        if (cleanPrefix.isEmpty()) {
            cleanPrefix = "profile_pictures";
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
    }

    private User withResolvedProfilePic(User user) {
        if (user == null) {
            return null;
        }

        String profilePic = user.getProfilePic();
        if (profilePic == null || profilePic.isBlank()) {
            return user;
        }

        if (profilePic.startsWith("http://") || profilePic.startsWith("https://") || profilePic.startsWith("/")) {
            return user;
        }

        try {
            requireS3Configured();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(profilePic)
                    .build();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .getObjectRequest(getObjectRequest)
                    .build();

            String presignedUrl = s3Presigner.presignGetObject(getObjectPresignRequest)
                    .url()
                    .toExternalForm();
            user.setProfilePic(presignedUrl);
        } catch (Exception ignored) {
        }

        return user;
    }

    private void deleteProfileImageIfManaged(String imageReference) {
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

    // User stats methods below

    public Map<String, Object> getUserStats(ObjectId userId) {
        User user = userDAO.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Post> posts = postDAO.findAllById(List.of(user.getPosts()));

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalSpottings", posts.size());
        stats.put("firstSightingDate", calculateFirstSighting(posts));
        stats.put("uniqueBirdsSpotted", calculateUniqueBirds(posts));
        stats.put("mostSpottedBird", calculateMostSpottedBird(posts));
        stats.put("topBirdsAllTime", topBirdsAllTime(posts));
        stats.put("topBirdsThisMonth", topBirdsThisMonth(posts));
        stats.put("badges", calculateBadges(stats));

        return stats;
    }

    private Date calculateFirstSighting(List<Post> posts) {
        return posts.stream()
                .map(Post::getTimestamp)
                .min(Date::compareTo)
                .orElse(null);
    }

    private int calculateUniqueBirds(List<Post> posts){
        return (int) posts.stream()
            .map(Post::getBird)
            .filter(Objects::nonNull)
            .distinct()
            .count();
    }

    private ObjectId calculateMostSpottedBird(List<Post> posts) {
        return posts.stream()
            .map(Post::getBird)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(b -> b, Collectors.counting()))
            .entrySet()
            .stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    private List<Map<String, Object>> topBirdsAllTime(List<Post> posts) {
        Map<ObjectId, Long> counts = posts.stream()
            .map(Post::getBird)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(b -> b, Collectors.counting()));

        return counts.entrySet().stream()
            .sorted(Map.Entry.<ObjectId, Long>comparingByValue().reversed())
            .map(entry -> {
                Map<String, Object> map = new HashMap<>();
                map.put("birdId", entry.getKey());
                map.put("count", entry.getValue());
                return map;
            })
            .toList();
    }


    private List<Map<String, Object>> topBirdsThisMonth(List<Post> posts) {
        YearMonth currentMonth = YearMonth.now();

        Map<ObjectId, Long> counts = posts.stream()
            .filter(post -> {
                LocalDate date = post.getTimestamp()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                    return YearMonth.from(date).equals(currentMonth);
            })
            .map(Post::getBird)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(b -> b, Collectors.counting()));

        return counts.entrySet().stream()
            .sorted(Map.Entry.<ObjectId, Long>comparingByValue().reversed())
            .map(entry -> {
                Map<String, Object> map = new HashMap<>();
                map.put("birdId", entry.getKey());
                map.put("count", entry.getValue());
                return map;
            })
            .toList();
    }

    public List<String> calculateBadges(Map<String, Object> stats) {
        List<String> badges = new ArrayList<>();

        int totalSpottings = (int) stats.getOrDefault("totalSpottings", 0);
        int uniqueBirds = (int) stats.getOrDefault("uniqueBirdsSpotted", 0);

        if (totalSpottings >= 1) {
            badges.add("First Sighting");
        }
        else if (uniqueBirds >= 10) {
            badges.add("Bird Collector");
        }
        else if (totalSpottings >= 50) {
            badges.add("Popular Spotter");
        }
        
        return badges;
    }
}
