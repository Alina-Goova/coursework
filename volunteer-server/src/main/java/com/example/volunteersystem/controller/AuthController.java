package com.example.volunteersystem.controller;

import com.example.volunteersystem.dto.UserDTO;
import com.example.volunteersystem.model.User;
import com.example.volunteersystem.security.JwtUtil;
import com.example.volunteersystem.service.UserService;
import com.example.volunteersystem.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8081")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Неверный email или пароль");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername(),
                userDetails.getAuthorities().iterator().next().getAuthority());

        User user = userService.getUserByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", convertToDTO(user));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Хешируем пароль
            String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());

            User user = userService.createUser(
                    registerRequest.getEmail(),
                    hashedPassword,
                    registerRequest.getName(),
                    registerRequest.getRole()
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(registerRequest.getEmail());
            final String jwt = jwtUtil.generateToken(userDetails.getUsername(),
                    userDetails.getAuthorities().iterator().next().getAuthority());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", convertToDTO(user));

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getName(),
                user.getPhoneNumber(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getRegistrationDate(),
                user.isActive()
        );
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        private String email;
        private String password;
        private String name;
        private com.example.volunteersystem.model.Role role;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public com.example.volunteersystem.model.Role getRole() { return role; }
        public void setRole(com.example.volunteersystem.model.Role role) { this.role = role; }
    }
}