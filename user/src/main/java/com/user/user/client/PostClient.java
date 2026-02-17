package com.user.user.client;

import com.user.user.models.Post;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "post")
public interface PostClient {
    
    @GetMapping("/sightings/{id}")
    Post getPostById(@PathVariable("id") String id);
    
    @GetMapping("/sightings/batch")
    List<Post> getPostsByIds(List<String> ids);
    
    @GetMapping("/sightings/user/{userId}")
    List<Post> getPostsByUserId(@PathVariable("userId") String userId);
}
