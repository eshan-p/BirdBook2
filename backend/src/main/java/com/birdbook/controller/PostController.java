package com.birdbook.controller;

import com.birdbook.models.Comment;
import com.birdbook.models.Post;
import com.birdbook.service.PostService;
import com.birdbook.service.PostUserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
@RestController
@RequestMapping("/sightings")
public class PostController {

    private final PostService sService;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final PostUserService puService;

    public PostController(PostService sightService, ObjectMapper objectMapper, Validator validator, PostUserService puService) {
        this.sService = sightService;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.puService = puService;
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return sService.getAllPosts();
    }

    @GetMapping("/user/{userId}")
    public List<Post> getAllPostsByFriends(@PathVariable ObjectId userId) {
        return sService.getAllPostsByFriends(userId);
    }

    @GetMapping("/tags")
    public List<Post> getAllPostsByTags(@RequestParam Map<String,String> tags) {
        return sService.getAllPostsByTags(tags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable ObjectId id) {
        return sService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/group/{groupId}")
    public List<Post> getAllPostsByGroup(@PathVariable ObjectId groupId) {
        return sService.getAllPostsByGroup(groupId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPost(
            @RequestPart("post") String postJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestParam String userId
    ) {
        try {
            Post post = objectMapper.readValue(postJson, Post.class);
            post.setUser(puService.buildPostUser(new ObjectId(userId)));

            Set<ConstraintViolation<Post>> violations = validator.validate(post);
            if (!violations.isEmpty()) {
                Map<String, String> errors = new HashMap<>();
                for (ConstraintViolation<Post> v : violations) {
                    errors.put(v.getPropertyPath().toString(), v.getMessage());
                }
                return ResponseEntity.badRequest().body(errors);
            }

            return ResponseEntity.ok(sService.createPost(post, image));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN_USER') or hasRole('SUPER_USER') or @postService.isOwner(#id, authentication.principal.userId)")
    public ResponseEntity<?> updatePostMultipart(
            @PathVariable("id") ObjectId id,
            @RequestPart("post") String postJson,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            Post updatedPost = objectMapper.readValue(postJson, Post.class);
            return ResponseEntity.ok(sService.updatePost(id, updatedPost, image));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update post");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_USER') or hasRole('SUPER_USER') or @postService.isOwner(#id, authentication.principal.userId)")
    public ResponseEntity<String> deletePost(@PathVariable String id){
        ObjectId pId = new ObjectId(id);
        sService.deletePostById(pId);
        return ResponseEntity.ok("Post deleted successfully");
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addComment(
            @PathVariable ObjectId id,
            @RequestBody Comment comment,
            @RequestParam String userId
    ) {
        comment.setUser(puService.buildPostUser(new ObjectId(userId)));
        comment.setTimestamp(new Date());
        return ResponseEntity.ok(sService.addComment(id, comment));
    }

    @PatchMapping("/{id}/comments")
    @PreAuthorize("@postService.isOwner(#id, authentication.principal.userId)")
    public ResponseEntity<?> updateComment(@PathVariable ObjectId id, @RequestBody Comment updatedComment) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }

            ObjectId userId = new ObjectId(auth.getName());
            return ResponseEntity.ok(sService.updateComment(id, userId, updatedComment));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/comments")
    @PreAuthorize("@postService.isOwner(#id, authentication.principal.userId)")
    public ResponseEntity<?> deleteComment(@PathVariable ObjectId id, @RequestBody Comment comment) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }

            ObjectId userId = new ObjectId(auth.getName());
            return ResponseEntity.ok(
                    sService.deleteComment(id, userId, comment.getTimestamp())
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{postId}/like/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Post> likePost(@PathVariable String postId, @PathVariable String userId) {

        ObjectId postObjId = new ObjectId(postId);
        ObjectId userObjId = new ObjectId(userId);
        return ResponseEntity.ok(sService.likePost(postObjId, userObjId));
    }

    @PutMapping("/{postId}/unlike/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Post> unlikePost(@PathVariable String postId, @PathVariable String userId) {

        ObjectId postObjId = new ObjectId(postId);
        ObjectId userObjId = new ObjectId(userId);
        return ResponseEntity.ok(sService.unlikePost(postObjId, userObjId));
    }

    @PutMapping("/{postId}/flag")
    @PreAuthorize("hasRole('ADMIN_USER') or hasRole('SUPER_USER')")
    public ResponseEntity<Post> flagPost(@PathVariable String postId) {

        ObjectId postObjId = new ObjectId(postId);
        return ResponseEntity.ok(sService.flagPost(postObjId));
    }

    @PutMapping("/{postId}/unflag")
    @PreAuthorize("hasRole('ADMIN_USER') or hasRole('SUPER_USER')")
    public ResponseEntity<Post> unflagPost(@PathVariable String postId) {

        ObjectId postObjId = new ObjectId(postId);
        return ResponseEntity.ok(sService.unflagPost(postObjId));
    }

    @PutMapping("/{postId}/help")
    @PreAuthorize("hasRole('ADMIN_USER') or hasRole('SUPER_USER') or @postService.isOwner(#id, authentication.principal.userId)")
    public ResponseEntity<Post> markNeedsHelp(@PathVariable String postId) {

        ObjectId postObjId = new ObjectId(postId);
        return ResponseEntity.ok(sService.markNeedsHelp(postObjId));
    }

    @PutMapping("/{postId}/help/remove")
    @PreAuthorize("hasRole('ADMIN_USER') or hasRole('SUPER_USER') or @postService.isOwner(#id, authentication.principal.userId)")
    public ResponseEntity<Post> removeHelpFlag(@PathVariable String postId) {

        ObjectId postObjId = new ObjectId(postId);
        return ResponseEntity.ok(sService.removeHelpFlag(postObjId));
    }

    @GetMapping("/{postId}/likes")
    public ResponseEntity<List<Map<String, String>>> getUsersWhoLiked(@PathVariable String postId) {
        
        ObjectId postObjId = new ObjectId(postId);
        return ResponseEntity.ok(sService.getUsersWhoLiked(postObjId));
    }
}
