package com.birdbook.group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.birdbook.group.models.Group;
import com.birdbook.group.models.PostUser;
import com.birdbook.group.service.GroupService;
import com.birdbook.group.service.PostUserService;

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
import org.springframework.security.access.prepost.PreAuthorize;

@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
@RestController
@RequestMapping("/groups")
public class GroupController {
    private final GroupService groupService;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final PostUserService puService;

    public GroupController(GroupService groupService, ObjectMapper objectMapper, Validator validator, PostUserService puService) {
        this.groupService = groupService;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.puService = puService;
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

    /*
    @PostMapping
    @PreAuthorize("hasRole('ADMIN_USER') or hasRole('SUPER_USER')")
    public ResponseEntity<String> createGroup(@RequestParam String name, @RequestParam String ownerId) {
        PostUser owner = puService.buildPostUser(new ObjectId(ownerId));
        groupService.createGroup(name, owner);
        return ResponseEntity.status(HttpStatus.CREATED).body("Group created successfully");
    }
    */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createGroup(
        @RequestPart("group") String groupJson,
        @RequestPart(value = "image", required = false) MultipartFile image,
        @RequestParam String userId
    ) {
        try {
            Group group = objectMapper.readValue(groupJson, Group.class);
            group.setOwner(puService.buildPostUser(new ObjectId(userId)));
            Set<ConstraintViolation<Group>> voilations = validator.validate(group);
            if(!voilations.isEmpty()){
                Map<String, String> errors = new HashMap<>();
                for(ConstraintViolation<Group> v : voilations) {
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> requestToJoin(
            @PathVariable String groupId,
            @RequestParam String userId
    ) {
        PostUser user = puService.buildPostUser(new ObjectId(userId));
        groupService.userRequestToJoin(user, new ObjectId(groupId));
        return ResponseEntity.ok("Join request sent");
    }

    @PutMapping("/{groupId}/join-requests/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN_USER') or hasRole('SUPER_USER')")
    public ResponseEntity<String> approveJoin(
            @PathVariable String groupId,
            @PathVariable String userId
    ) {
        PostUser user = puService.buildPostUser(new ObjectId(userId));
        groupService.approveJoinRequest(user, new ObjectId(groupId));
        return ResponseEntity.ok("Join request approved");
    }

    @PutMapping("/{groupId}/join-requests/{userId}/deny")
    @PreAuthorize("hasRole('ADMIN_USER') or hasRole('SUPER_USER')")
    public ResponseEntity<String> denyJoinRequest(@PathVariable String groupId, @PathVariable String userId) {

        ObjectId groupObjId = new ObjectId(groupId);
        ObjectId userObjId = new ObjectId(userId);
        groupService.denyJoinRequest(puService.buildPostUser(userObjId), groupObjId);

        return new ResponseEntity<String>("Join request denied successfully", HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateGroup(
            @PathVariable String id, 
            @RequestBody Group groupRequest,
            @RequestParam String userId
    ) {
        ObjectId groupObjId = new ObjectId(id);
        Group group = groupService.getGroupById(groupObjId);
        
        // Only owner can edit
        if (!group.getOwner().getUserId().equals(new ObjectId(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Only the group owner can edit this group");
        }
        
        Group updatedGroup = groupService.updateGroup(groupObjId, groupRequest);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteGroup(
            @PathVariable String id,
            @RequestParam String userId
    ) {
        ObjectId groupObjId = new ObjectId(id);
        Group group = groupService.getGroupById(groupObjId);
        
        // Only owner can delete
        if (!group.getOwner().getUserId().equals(new ObjectId(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Only the group owner can delete this group");
        }
        
        groupService.deleteGroup(groupObjId);
        return ResponseEntity.ok("Group deleted successfully");
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> leaveGroup(@PathVariable String groupId, @PathVariable String userId) {
        
        ObjectId groupObjId = new ObjectId(groupId);
        ObjectId userObjId = new ObjectId(userId);
        groupService.removeGroupMember(userObjId, groupObjId);
        return ResponseEntity.ok("Left group successfully");
    }

    @DeleteMapping("/{groupId}/members/{userId}/remove")
    @PreAuthorize("hasRole('ADMIN_USER') or hasRole('SUPER_USER')")
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
