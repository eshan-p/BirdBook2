package com.birdbook.group.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.birdbook.group.models.Group;
import com.birdbook.group.models.PostUser;
import com.birdbook.group.repository.GroupDAO;

@Service
public class GroupService {

    private final GroupDAO groupDAO;

    public GroupService(GroupDAO groupDAO) {
        this.groupDAO = groupDAO;
    }

    public List<Group> getAllGroups() {
        return groupDAO.findAll();
    }

    public Group getGroupById(ObjectId groupId) {
        return groupDAO.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found."));
    }

    public Group createGroup(Group newGroup, MultipartFile imageFile) {
        if(imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            newGroup.setImage(imagePath);
        }
        return groupDAO.save(newGroup);
    }

    public Group updateGroup(ObjectId groupId, Group updatedData) {
        Group group = getGroupById(groupId);
        group.setName(updatedData.getName());
        if (updatedData.getDescription() != null) {
            group.setDescription(updatedData.getDescription());
        }
        return groupDAO.save(group);
    }

    public void deleteGroup(ObjectId groupId) {
        if (!groupDAO.existsById(groupId)) {
            throw new IllegalArgumentException("Group not found.");
        }
        groupDAO.deleteById(groupId);
    }

    private String saveImage(MultipartFile imageFile){
        try {
            String uploadDir = "images";
            Files.createDirectories(Paths.get(uploadDir));

            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            Files.copy(imageFile.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            return "/" + uploadDir + "/" + fileName;

        } catch (IOException e){
            throw new RuntimeException("Failed to store image", e);
        }
    }

    /* =========================
       MEMBERS / REQUESTS
       ========================= */

    public List<PostUser> getRequestedUsers(ObjectId groupId) {
        return getGroupById(groupId).getRequests();
    }

    public List<PostUser> getGroupMembers(ObjectId groupId) {
        return getGroupById(groupId).getMembers();
    }

    public void userRequestToJoin(PostUser user, ObjectId groupId) {
        Group group = getGroupById(groupId);

        boolean alreadyRequested = group.getRequests().stream()
                .anyMatch(u -> u.getUserId().equals(user.getUserId()));

        if (alreadyRequested) {
            throw new IllegalArgumentException("Request already sent.");
        }

        boolean alreadyMember = group.getMembers().stream()
                .anyMatch(u -> u.getUserId().equals(user.getUserId()));

        if (alreadyMember) {
            throw new IllegalArgumentException("User is already a member.");
        }

        group.getRequests().add(user);
        groupDAO.save(group);
    }

    public void approveJoinRequest(PostUser user, ObjectId groupId) {
        Group group = getGroupById(groupId);

        boolean exists = group.getRequests().stream()
                .anyMatch(u -> u.getUserId().equals(user.getUserId()));

        if (!exists) {
            throw new IllegalArgumentException("No join request from this user.");
        }

        group.getRequests().removeIf(u -> u.getUserId().equals(user.getUserId()));
        group.getMembers().add(user);

        groupDAO.save(group);
    }

    public void denyJoinRequest(PostUser user, ObjectId groupId) {
        Group group = getGroupById(groupId);

        boolean exists = group.getRequests().stream()
                .anyMatch(u -> u.getUserId().equals(user.getUserId()));

        if (!exists) {
            throw new IllegalArgumentException("No join request from this user.");
        }

        group.getRequests().removeIf(u -> u.getUserId().equals(user.getUserId()));
        groupDAO.save(group);
    }

    public void removeGroupMember(ObjectId userId, ObjectId groupId) {
        Group group = getGroupById(groupId);
        
        boolean removed = group.getMembers().removeIf(u -> u.getUserId().equals(userId));
        
        if (!removed) {
            throw new IllegalArgumentException("User is not a member of this group");
        }
        
        groupDAO.save(group);
    }
}
