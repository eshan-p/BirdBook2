package com.user.user.repository;

import com.user.user.client.GroupClient;
import com.user.user.models.Group;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository adapter for Group microservice
 * Wraps Feign client to provide DAO-like interface
 */
@Repository
public class GroupDAO {
    
    private final GroupClient groupClient;
    
    public GroupDAO(GroupClient groupClient) {
        this.groupClient = groupClient;
    }
    
    public Optional<Group> findById(ObjectId id) {
        try {
            Group group = groupClient.getGroupById(id.toHexString());
            return Optional.ofNullable(group);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public List<Group> findAllById(List<ObjectId> ids) {
        try {
            List<String> hexIds = ids.stream()
                    .map(ObjectId::toHexString)
                    .collect(Collectors.toList());
            return groupClient.getGroupsByIds(hexIds);
        } catch (Exception e) {
            return List.of();
        }
    }
}
