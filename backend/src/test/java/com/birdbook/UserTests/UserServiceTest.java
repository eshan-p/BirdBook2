package com.birdbook.UserTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.birdbook.models.Role;
import com.birdbook.models.User;
import com.birdbook.repository.GroupDAO;
import com.birdbook.repository.PostDAO;
import com.birdbook.repository.UserDAO;
import com.birdbook.service.UserService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserDAO userDAO;

    @Mock
    private PostDAO postDAO;

    @Mock
    private GroupDAO groupDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void getAllUsers_Success(){
        userService.getAllUsers();
        verify(userDAO, times(1)).findAll();
    }

    @Test
    public void getUserById_Success(){

        ObjectId id = new ObjectId();

        when(userDAO.findById(id)).thenReturn(Optional.of(new User("testuser", "password")));

        userService.getUserById(id);

        verify(userDAO, times(1)).findById(id);
    }

    @Test
    public void getUserById_UserNotFound(){

        ObjectId id = new ObjectId();

        when(userDAO.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(id));
        verify(userDAO, times(1)).findById(id);
    }

    @Test
    public void registerUser_Success(){

        String username = "newuser";
        String password = "pass123";
        String hashedPassword = "hashed_pass123";

        when(userDAO.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);

        User result = userService.registerUser(username, password);

        verify(passwordEncoder, times(1)).encode(password);
        verify(userDAO, times(1)).insert(any(User.class));
        
        assertEquals(username, result.getUsername());
        assertEquals(hashedPassword, result.getPassword());
        assertEquals(Role.BASIC_USER, result.getRole());
        assertNotNull(result.getFriends());
        assertNotNull(result.getGroups());
        assertNotNull(result.getPosts());
    }

    @Test
    public void registerUser_UsernameTaken(){

        String username = "existinguser";
        String password = "pass123";

        when(userDAO.findByUsername(username)).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(username, password));
        verify(userDAO, times(0)).insert(any(User.class));
    }

    @Test
    public void updateUser_Success(){

        ObjectId id = new ObjectId();
        User existingUser = new User("olduser", "oldpass");
        User updatedData = new User("updateduser", "newpass123");
        String hashedNewPassword = "hashed_newpass123";

        when(userDAO.findById(id)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newpass123")).thenReturn(hashedNewPassword);
        when(userDAO.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updateUser(id, updatedData, null);

        verify(passwordEncoder, times(1)).encode("newpass123");
        verify(userDAO, times(1)).save(existingUser);

        assertEquals("updateduser", existingUser.getUsername());
        assertEquals(hashedNewPassword, existingUser.getPassword());
    }

    @Test
    public void updateUser_UserNotFound(){

        ObjectId id = new ObjectId();
        User updatedData = new User("updateduser", "newpass123");

        when(userDAO.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(id, updatedData, null));
        verify(userDAO, times(0)).save(any(User.class));
    }

    @Test
    public void deleteUser_Success(){

        ObjectId id = new ObjectId();

        when(userDAO.existsById(id)).thenReturn(true);

        userService.deleteUser(id);

        verify(userDAO, times(1)).deleteById(id);
    }

    @Test
    public void deleteUser_UserNotFound(){

        ObjectId id = new ObjectId();

        when(userDAO.existsById(id)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(id));
        verify(userDAO, times(0)).deleteById(id);
    }

    @Test
    public void addFriend_Success(){
        ObjectId userId = new ObjectId();
        ObjectId friendId = new ObjectId();
        ObjectId existingFriendId = new ObjectId();

        User user = new User("user1", "pass1");
        user.setFriends(new ObjectId[] {existingFriendId});
        User friend = new User("user2", "pass2");

        when(userDAO.findById(userId)).thenReturn(Optional.of(user));
        when(userDAO.findById(friendId)).thenReturn(Optional.of(friend));

        userService.addFriend(userId, friendId);

        verify(userDAO, times(1)).save(user);
        
        assertEquals(2, user.getFriends().length);
        assertEquals(existingFriendId, user.getFriends()[0]);
        assertEquals(friendId, user.getFriends()[1]);
    }

    @Test
    public void addFriend_FriendNotFound(){
        ObjectId userId = new ObjectId();
        ObjectId friendId = new ObjectId();

        User user = new User("user1", "pass1");
        user.setFriends(new ObjectId[] {});

        when(userDAO.findById(userId)).thenReturn(Optional.of(user));
        when(userDAO.findById(friendId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.addFriend(userId, friendId));
        verify(userDAO, times(0)).save(user);
    }

    @Test
    public void validateUsername_Invalid(){
        User user = new User("ab@", "Pass1!");
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    public void validatePassword_Invalid(){
        User user = new User("validuser", "pass1!");
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }
}