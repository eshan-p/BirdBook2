package com.birdbook.controller;

import java.util.List;
import java.util.Map;

import com.birdbook.models.*;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.birdbook.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserController(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<UserSummaryDTO> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(user -> new UserSummaryDTO(
                        user.getId().toHexString(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getLocation(),
                        user.getRole().name(),
                        user.getProfilePic(),
                        toHexArray(user.getFriends()),
                        toHexArray(user.getPosts()),
                        toHexArray(user.getGroups())
                ))
                .toList();
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserSummaryDTO> getUser(@PathVariable String id) {
        ObjectId userId = new ObjectId(id);
        User user = userService.getUserById(userId);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                new UserSummaryDTO(
                        user.getId().toHexString(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getLocation(),
                        user.getRole().name(),
                        user.getProfilePic(),
                        toHexArray(user.getFriends()),
                        toHexArray(user.getPosts()),
                        toHexArray(user.getGroups())
                )
        );
    }


    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String query) {
        return userService.searchUsersByUsername(query);
    }

    @GetMapping("/{id}/friends")
    public List<UserSummaryDTO> getFriends(@PathVariable String id) {
        return userService.getFriendsList(new ObjectId(id)).stream()
                .map(user -> new UserSummaryDTO(
                        user.getId().toHexString(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getLocation(),
                        user.getRole().name(),
                        user.getProfilePic(),
                        toHexArray(user.getFriends()),
                        toHexArray(user.getPosts()),
                        toHexArray(user.getGroups())
                ))
                .toList();
    }

    @GetMapping("/{id}/groups")
    public List<Group> getGroups(@PathVariable String id) {

        ObjectId userId = new ObjectId(id);
        return userService.getGroupsList(userId);
    }

    @GetMapping("/{id}/posts")
    public List<Post> getPosts(@PathVariable String id) {
        ObjectId userId = new ObjectId(id);
        return userService.getPostsList(userId);
    }

    @GetMapping("/{id}/top-birds")
    public List<Map<String, Object>> getTopBirdsSighted(@PathVariable String id) {
        ObjectId userId = new ObjectId(id);
        return userService.getTopBirdsThisMonth(userId);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable String id) {
        try {
            ObjectId userObjId = new ObjectId(id);
            Map<String, Object> stats = userService.getUserStats(userObjId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<String> registerUser(@Valid @RequestBody User userRequest){ 
        
        userService.registerUser(userRequest.getUsername(), userRequest.getPassword());

        return new ResponseEntity<String>("User registered successfully", HttpStatus.CREATED);
    }

    @PostMapping("/onboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> completeOnboarding(
        @RequestPart("firstName") String firstName,
        @RequestPart("lastName") String lastName,
        @RequestPart("location") String location,
        @RequestPart(value="profilePhoto", required = false) MultipartFile profilePhoto,
        HttpServletRequest request
    ) {
        System.out.println("HIT ONBOARDING");
        try{
            String userId = request.getUserPrincipal().getName();
            userService.completeOnboarding(userId, firstName, lastName, location, profilePhoto);
            return ResponseEntity.ok(Map.of("message", "Onboarding completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));
        }
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<String> addFriend(@PathVariable String id, @PathVariable String friendId) {

        ObjectId userId = new ObjectId(id);
        ObjectId friendIdObj = new ObjectId(friendId);
        userService.addFriend(userId, friendIdObj);

        return new ResponseEntity<String>("Friend added successfully", HttpStatus.OK);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> removeFriend(@PathVariable String id, @PathVariable String friendId) {
        ObjectId userId = new ObjectId(id);
        ObjectId friendIdObj = new ObjectId(friendId);
        userService.removeFriend(userId, friendIdObj);
        return ResponseEntity.ok("Friend removed successfully");
    }

    /*@PutMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User userRequest){

        ObjectId userId = new ObjectId(id);
        User updatedUser = userService.updateUser(userId, userRequest);

        return ResponseEntity.ok(updatedUser);
    }*/
    
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@userService.isOwner(#id, authentication.principal.userId)")
    public User updateUserMultipart(
        @PathVariable("id") ObjectId id,
        @RequestPart("user") String userJson,
        @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            User updatedUser = objectMapper.readValue(userJson, User.class);
            return userService.updateUser(id, updatedUser, image);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@userService.isOwner(#id, authentication.principal.userId)")
    public ResponseEntity<String> deleteUser(@PathVariable String id){

        ObjectId userId = new ObjectId(id);
        userService.deleteUser(userId);

        return new ResponseEntity<String>("User deleted successfully", HttpStatus.OK);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPER_USER')")
    public ResponseEntity<UserSummaryDTO> updateUserRole(@PathVariable String id, @RequestBody Map<String, String> roleUpdate) {
        try {
            ObjectId userId = new ObjectId(id);
            String newRole = roleUpdate.get("role");
            
            User updatedUser = userService.updateUserRole(userId, newRole);
            
            UserSummaryDTO userDTO = new UserSummaryDTO(
                updatedUser.getId().toHexString(),
                updatedUser.getUsername(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getLocation(),
                updatedUser.getRole().name(),
                updatedUser.getProfilePic(),
                toHexArray(updatedUser.getFriends()),
                toHexArray(updatedUser.getPosts()),
                toHexArray(updatedUser.getGroups())
            );
            
            return ResponseEntity.ok(userDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String[] toHexArray(ObjectId[] ids) {
        if (ids == null) return new String[0];

        return java.util.Arrays.stream(ids)
                .map(ObjectId::toHexString)
                .toArray(String[]::new);
    }
}
