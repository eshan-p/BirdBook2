package com.birdbook.group.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.Map;

@FeignClient(name = "user")
public interface UserClient {
    @GetMapping("/users/{id}")
    Map<String, Object> getUser(@PathVariable("id") String id);
    
    @PutMapping("/users/{userId}/groups/{groupId}")
    ResponseEntity<String> addGroup(@PathVariable("userId") String userId, @PathVariable("groupId") String groupId);
}