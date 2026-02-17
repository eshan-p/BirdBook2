package com.user.user.client;

import com.user.user.models.Bird;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "bird")
public interface BirdClient {
    
    @GetMapping("/birds/{id}")
    Bird getBirdById(@PathVariable("id") String id);
}
