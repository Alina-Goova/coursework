package com.example.volunteersystem.controller;

import com.example.volunteersystem.model.User;
import com.example.volunteersystem.model.Role;
import com.example.volunteersystem.dto.UserDTO;
import com.example.volunteersystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

import com.example.volunteersystem.repository.RegistrationRepository;
import com.example.volunteersystem.model.Event;
import com.example.volunteersystem.dto.EventDTO;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:8081") // Для связи с JavaFX клиентом
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    private EventDTO convertToEventDTO(Event event) {
        return new EventDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getStartDate(),
                event.getEndDate(),
                event.getMaxParticipants(),
                event.getCurrentParticipants(),
                event.getStatus(),
                event.getCategory(),
                event.getOrganizer() != null ? event.getOrganizer().getId() : null,
                event.getOrganizer() != null ? event.getOrganizer().getName() : null,
                event.isHidden()
        );
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<List<EventDTO>> getUserEvents(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> {
                    List<EventDTO> events = registrationRepository.findByUser(user).stream()
                            .map(registration -> convertToEventDTO(registration.getEvent()))
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(events);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(convertToDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequest request) {
        try {
            String hashedPassword = passwordEncoder.encode(request.getPasswordHash());
            User user = userService.createUser(
                    request.getEmail(),
                    hashedPassword,
                    request.getName(),
                    request.getRole()
            );
            return ResponseEntity.ok(convertToDTO(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        try {
            User user = userService.updateUser(id, request.getName(), request.getEmail(), request.getPhoneNumber(), request.getBio(), request.getAvatarUrl());
            return ResponseEntity.ok(convertToDTO(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserDTO> updateUserStatus(@PathVariable Long id, @RequestBody UserStatusRequest request) {
        try {
            User user = userService.updateUserStatus(id, request.isActive());
            return ResponseEntity.ok(convertToDTO(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserDTO> updateUserRole(@PathVariable Long id, @RequestBody UserRoleRequest request) {
        try {
            User user = userService.updateUserRole(id, request.getRole());
            return ResponseEntity.ok(convertToDTO(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/admin-change-password")
    public ResponseEntity<?> adminChangePassword(@PathVariable Long id, @RequestBody AdminChangePasswordRequest request) {
        try {
            userService.adminChangePassword(id, request.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
             return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Вспомогательные классы для запросов
    public static class UserCreateRequest {
        private String email;
        private String passwordHash;
        private String name;
        private Role role;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPasswordHash() { return passwordHash; }
        public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
    }

    public static class UserUpdateRequest {
        private String name;
        private String email;
        private String phoneNumber;
        private String bio;
        private String avatarUrl;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }

    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class UserStatusRequest {
        private boolean active;
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    public static class UserRoleRequest {
        private Role role;
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
    }

    public static class AdminChangePasswordRequest {
        private String newPassword;
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}