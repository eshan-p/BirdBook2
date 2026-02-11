package com.birdbook.controller;

import com.birdbook.service.SearchService;

import org.springframework.web.bind.annotation.*;

import com.birdbook.models.Bird;
import com.birdbook.models.Group;
import com.birdbook.models.Post;
import com.birdbook.models.User;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
@RestController
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> searchAll(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> results = searchService.searchAll(query.trim());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/birds")
    public ResponseEntity<List<Bird>> searchBirds(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(searchService.searchBirds(query.trim()));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        if(query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(searchService.searchUsers(query.trim()));
    }

    @GetMapping("/friends")
    public ResponseEntity<List<User>> searchFriends(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        
        ObjectId userId = new ObjectId(auth.getName());
        return ResponseEntity.ok(searchService.searchFriends(query.trim(), userId));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> searchPosts(@RequestParam String query) {
        if(query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(searchService.searchPosts(query.trim()));
    }

    @GetMapping("/groups")
    public ResponseEntity<List<Group>> searchGroups(@RequestParam String query) {
        if(query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(searchService.searchGroups(query.trim()));
    }

    @GetMapping("/my-groups")
    public ResponseEntity<List<Group>> searchUserGroups(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        
        ObjectId userId = new ObjectId(auth.getName());
        return ResponseEntity.ok(searchService.searchUserGroups(query.trim(), userId));
    }
}
