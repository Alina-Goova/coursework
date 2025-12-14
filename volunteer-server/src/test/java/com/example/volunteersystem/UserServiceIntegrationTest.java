package com.example.volunteersystem;

import com.example.volunteersystem.model.Role;
import com.example.volunteersystem.model.User;
import com.example.volunteersystem.service.UserService;
import com.example.volunteersystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testChangePasswordAfterUpdate() {
        // 1. Create User
        String originalPassword = "password123";
        String encodedPassword = passwordEncoder.encode(originalPassword);
        // Using a unique email to avoid conflicts if DB is not empty
        String email = "integration_test_" + System.currentTimeMillis() + "@example.com";
        
        User user = userService.createUser(email, encodedPassword, "Test User", Role.VOLUNTEER);
        Long userId = user.getId();

        System.out.println("Test User Created: " + user.getId());

        // Verify initial login check (simulated)
        assertTrue(passwordEncoder.matches(originalPassword, user.getPasswordHash()));

        // 2. Update Profile (mimic what the controller does)
        // Note: userService.updateUser takes params: id, name, email, phone, bio, avatar
        userService.updateUser(userId, "Updated Name", email, "1234567890", "Bio", "avatar.png");
        
        // Reload user from DB to be sure
        User updatedUser = userRepository.findById(userId).orElseThrow();
        
        System.out.println("User Updated. Hash: " + updatedUser.getPasswordHash());

        // Check if password hash is still valid
        assertTrue(passwordEncoder.matches(originalPassword, updatedUser.getPasswordHash()), 
                   "Password should still match after profile update");

        // 3. Change Password
        String newPassword = "newPassword456";
        userService.changePassword(userId, originalPassword, newPassword);

        // 4. Verify new password
        User finalUser = userRepository.findById(userId).orElseThrow();
        assertTrue(passwordEncoder.matches(newPassword, finalUser.getPasswordHash()));
        assertFalse(passwordEncoder.matches(originalPassword, finalUser.getPasswordHash()));
    }
}
