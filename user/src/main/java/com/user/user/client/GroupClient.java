package com.user.user.client;

import com.user.user.models.Group;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "group")
public interface GroupClient {
    
    @GetMapping("/groups/{id}")
    Group getGroupById(@PathVariable("id") String id);
    
    @GetMapping("/groups/batch")
    List<Group> getGroupsByIds(List<String> ids);
}
