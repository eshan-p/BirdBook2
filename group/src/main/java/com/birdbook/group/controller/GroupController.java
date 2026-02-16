package com.birdbook.group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.birdbook.group.models.Group;
import com.birdbook.group.models.PostUser;
import com.birdbook.group.service.GroupService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/groups")
public class GroupController {
    private final GroupService groupService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public GroupController(GroupService groupService, ObjectMapper objectMapper, Validator validator) {
        this.groupService = groupService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @GetMapping
    public List<Group> getAllGroups() {
        return groupService.getAllGroups();
    }

    @GetMapping("/{id}")
    public Group getGroup(@PathVariable String id) {
        
        ObjectId groupObjId = new ObjectId(id);
        return groupService.getGroupById(groupObjId);
    }

    @GetMapping("/{groupId}/join-requests")
    public List<PostUser> getRequests(@PathVariable String groupId) {
        return groupService.getRequestedUsers(new ObjectId(groupId));
    }

    @GetMapping("/{groupId}/members")
    public List<PostUser> getMembers(@PathVariable String groupId) {
        return groupService.getGroupMembers(new ObjectId(groupId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createGroup(
        @RequestPart("group") String groupJson,
        @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            Group group = objectMapper.readValue(groupJson, Group.class);
            
            // Validate the group
            Set<ConstraintViolation<Group>> violations = validator.validate(group);
            if(!violations.isEmpty()){
                Map<String, String> errors = new HashMap<>();
                for(ConstraintViolation<Group> v : violations) {
                    errors.put(v.getPropertyPath().toString(), v.getMessage());
                }
                return ResponseEntity.badRequest().body(errors);
            }
            
            return ResponseEntity.ok(groupService.createGroup(group, image));
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}/join-requests")
    public ResponseEntity<String> requestToJoin(
            @PathVariable String groupId,
            @RequestBody PostUser user
    ) {
        groupService.userRequestToJoin(user, new ObjectId(groupId));
        return ResponseEntity.ok("Join request sent");
    }

    @PutMapping("/{groupId}/join-requests/{userId}/approve")
    public ResponseEntity<String> approveJoin(
            @PathVariable String groupId,
            @PathVariable String userId,
            @RequestBody PostUser user
    ) {
        groupService.approveJoinRequest(user, new ObjectId(groupId));
        return ResponseEntity.ok("Join request approved");
    }

    @PutMapping("/{groupId}/join-requests/{userId}/deny")
    public ResponseEntity<String> denyJoinRequest(
            @PathVariable String groupId, 
            @PathVariable String userId,
            @RequestBody PostUser user
    ) {
        ObjectId groupObjId = new ObjectId(groupId);
        groupService.denyJoinRequest(user, groupObjId);

        return new ResponseEntity<String>("Join request denied successfully", HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(
            @PathVariable String id, 
            @RequestBody Group groupRequest
    ) {
        ObjectId groupObjId = new ObjectId(id);
        Group updatedGroup = groupService.updateGroup(groupObjId, groupRequest);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGroup(@PathVariable String id) {
        ObjectId groupObjId = new ObjectId(id);
        groupService.deleteGroup(groupObjId);
        return ResponseEntity.ok("Group deleted successfully");
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<String> removeMember(
            @PathVariable String groupId, 
            @PathVariable String userId
    ) {
        ObjectId groupObjId = new ObjectId(groupId);
        ObjectId userObjId = new ObjectId(userId);
        groupService.removeGroupMember(userObjId, groupObjId);
        return ResponseEntity.ok("Member removed successfully");
    }
}
