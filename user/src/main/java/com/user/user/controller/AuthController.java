package com.user.user.controller;

import com.user.user.models.Role;
import com.user.user.models.User;
import com.user.user.security.JwtUtil;
import com.user.user.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;

@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletResponse response)  {

        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Username and password are required"));
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        org.springframework.security.core.userdetails.User springUser =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        User user = userService
                .getByUsername(springUser.getUsername())
                .orElseThrow();

        String token = jwtUtil.generateToken(
                user.getId().toHexString(),
                user.getUsername(),
                user.getRole().name()
        );
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true ONLY if using HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 7); // 7 days
        cookie.setDomain("localhost");

        // Required so browser sends cookie on refresh
        cookie.setAttribute("SameSite", "Lax");

        response.addCookie(cookie);

        return ResponseEntity.ok(formatUserResponse(user));
    }


    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody Map<String, String> body,
            HttpServletResponse response
    ) {

        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Username and password are required"));
        }

        if (userService.getByUsername(username).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already exists"));
        }

        //Create user
        User user = userService.registerUser(username, password);

        //Generate JWT (same as login)
        String token = jwtUtil.generateToken(
                user.getId().toHexString(),
                user.getUsername(),
                user.getRole().name()
        );

        //Store JWT in cookie
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true in prod (https)
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 7); // 7 days
        cookie.setDomain("localhost");

        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.CREATED).body(formatUserResponse(user));
    }


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String token = null;

        if(cookies != null){
            for(Cookie cookie : cookies) {
                if("jwt".equals(cookie.getName())){
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if(token == null || token.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","No JWT token found"));
        }

        try{
            String userId = jwtUtil.extractUserId(token);
            User user = userService.getUserById(new ObjectId(userId));
            if(user == null){
                return ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "User not found"));
            }
            return ResponseEntity.ok(formatUserResponse(user));
        } catch (Exception e) {
            return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setDomain("localhost");
        cookie.setAttribute("SameSite", "Lax");

        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    private Map<String, Object> formatUserResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId().toHexString());
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("profilePic", user.getProfilePic());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("location", user.getLocation());
        response.put("onboardingComplete", user.getOnboardingComplete());
        return response;
    }
}