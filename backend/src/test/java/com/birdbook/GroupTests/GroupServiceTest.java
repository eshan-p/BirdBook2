package com.birdbook.GroupTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.birdbook.models.Group;
import com.birdbook.models.PostUser;
import com.birdbook.models.User;
import com.birdbook.repository.GroupDAO;
import com.birdbook.repository.UserDAO;
import com.birdbook.service.GroupService;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {
    
    @Mock
    private GroupDAO groupDAO;

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private GroupService groupService;

    private Group testGroup;
    private User testUser;
    private PostUser testOwner;
    private PostUser testMember;
    private ObjectId groupId;
    private ObjectId ownerId;

    @BeforeEach
    void setup(){
        groupId = new ObjectId();
        ownerId = new ObjectId();
        testOwner = new PostUser(ownerId, "owner");
        testMember = new PostUser(new ObjectId(), "member");
        
        testGroup = new Group("Test Group", testOwner);
        testGroup.setId(groupId);
        
        testUser = new User();
        testUser.setId(ownerId);
        testUser.setUsername("owner");
        testUser.setGroups(new ObjectId[0]);
    }

    @Test
    public void getAllGroups_Success(){
        when(groupDAO.findAll()).thenReturn(List.of(testGroup));

        List<Group> result = groupService.getAllGroups();

        assertEquals(1, result.size());
        verify(groupDAO, times(1)).findAll();
    }

    @Test
    public void getGroupById_Success(){
        when(groupDAO.findById(groupId)).thenReturn(Optional.of(testGroup));

        Group result = groupService.getGroupById(groupId);

        assertEquals("Test Group", result.getName());
        verify(groupDAO, times(1)).findById(groupId);
    }

    @Test
    public void getGroupById_NotFound(){
        when(groupDAO.findById(groupId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> groupService.getGroupById(groupId));
    }

    @Test
    public void createGroup_Success(){
        when(groupDAO.save(any(Group.class))).thenReturn(testGroup);
        when(userDAO.findById(ownerId)).thenReturn(Optional.of(testUser));
        when(userDAO.save(any(User.class))).thenReturn(testUser);

        Group result = groupService.createGroup(testGroup, null);

        verify(groupDAO, times(1)).save(testGroup);
        
        verify(userDAO, times(1)).findById(ownerId);
        verify(userDAO, times(1)).save(testUser);
        
        assertEquals(1, testUser.getGroups().length);
        assertEquals(groupId, testUser.getGroups()[0]);
    }

    @Test
    public void updateGroup_UpdatesNameAndDescription(){
        Group updatedGroup = new Group("Updated Name", testOwner);
        updatedGroup.setDescription("New description");

        when(groupDAO.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(groupDAO.save(any(Group.class))).thenReturn(testGroup);

        Group result = groupService.updateGroup(groupId, updatedGroup);

        verify(groupDAO, times(1)).save(testGroup);
        assertEquals("Updated Name", testGroup.getName());
        assertEquals("New description", testGroup.getDescription());
    }

    @Test
    public void deleteGroup_Success(){
        when(groupDAO.existsById(groupId)).thenReturn(true);

        groupService.deleteGroup(groupId);

        verify(groupDAO, times(1)).deleteById(groupId);
    }

    @Test
    public void deleteGroup_NotFound(){
        when(groupDAO.existsById(groupId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> groupService.deleteGroup(groupId));
        verify(groupDAO, times(0)).deleteById(groupId);
    }

    @Test
    public void userRequestToJoin_Success(){
        when(groupDAO.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(groupDAO.save(any(Group.class))).thenReturn(testGroup);

        groupService.userRequestToJoin(testMember, groupId);

        verify(groupDAO, times(1)).save(testGroup);

        assertEquals(1, testGroup.getRequests().size());
        assertTrue(testGroup.getRequests().stream()
            .anyMatch(u -> u.getUserId().equals(testMember.getUserId())));
    }

    @Test
    public void userRequestToJoin_AlreadyRequested(){
        testGroup.getRequests().add(testMember);

        when(groupDAO.findById(groupId)).thenReturn(Optional.of(testGroup));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> groupService.userRequestToJoin(testMember, groupId)
        );
        
        assertEquals("Request already sent.", exception.getMessage());
        verify(groupDAO, never()).save(any(Group.class));
    }

    @Test
    public void userRequestToJoin_AlreadyMember(){
        testGroup.getMembers().add(testMember);

        when(groupDAO.findById(groupId)).thenReturn(Optional.of(testGroup));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> groupService.userRequestToJoin(testMember, groupId)
        );
        
        assertEquals("User is already a member.", exception.getMessage());
        verify(groupDAO, never()).save(any(Group.class));
    }

    @Test
    public void approveJoinRequest_Success(){
        testGroup.getRequests().add(testMember);

        when(groupDAO.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(groupDAO.save(any(Group.class))).thenReturn(testGroup);

        groupService.approveJoinRequest(testMember, groupId);

        verify(groupDAO, times(1)).save(testGroup);

        assertEquals(0, testGroup.getRequests().size());
        assertEquals(1, testGroup.getMembers().size());
        assertTrue(testGroup.getMembers().stream()
            .anyMatch(u -> u.getUserId().equals(testMember.getUserId())));
        assertFalse(testGroup.getRequests().stream()
            .anyMatch(u -> u.getUserId().equals(testMember.getUserId())));
    }

    @Test
    public void approveJoinRequest_NoRequest(){
        when(groupDAO.findById(groupId)).thenReturn(Optional.of(testGroup));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> groupService.approveJoinRequest(testMember, groupId)
        );
        
        assertEquals("No join request from this user.", exception.getMessage());
    }

    @Test
    public void denyJoinRequest_Success(){
        testGroup.getRequests().add(testMember);

        when(groupDAO.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(groupDAO.save(any(Group.class))).thenReturn(testGroup);

        groupService.denyJoinRequest(testMember, groupId);

        verify(groupDAO, times(1)).save(testGroup);
        
        assertEquals(0, testGroup.getRequests().size());
        assertEquals(0, testGroup.getMembers().size());
        assertFalse(testGroup.getRequests().stream()
            .anyMatch(u -> u.getUserId().equals(testMember.getUserId())));
    }

    @Test
    public void removeGroupMember_Success(){
        testGroup.getMembers().add(testMember);

        when(groupDAO.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(groupDAO.save(any(Group.class))).thenReturn(testGroup);

        groupService.removeGroupMember(testMember.getUserId(), groupId);

        verify(groupDAO, times(1)).save(testGroup);
        
        assertEquals(0, testGroup.getMembers().size());
        assertFalse(testGroup.getMembers().stream()
            .anyMatch(u -> u.getUserId().equals(testMember.getUserId())));
    }

    @Test
    public void removeGroupMember_NotMember(){
        when(groupDAO.findById(groupId)).thenReturn(Optional.of(testGroup));

        // Verify service layer validates user is a member before removal
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> groupService.removeGroupMember(testMember.getUserId(), groupId)
        );
        
        assertEquals("User is not a member of this group", exception.getMessage());
    }
}