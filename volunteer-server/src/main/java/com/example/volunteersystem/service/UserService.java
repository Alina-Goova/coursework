package com.example.volunteersystem.service;

import com.example.volunteersystem.model.User;
import com.example.volunteersystem.model.Role;
import com.example.volunteersystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.volunteersystem.repository.RegistrationRepository registrationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserService.class);

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createUser(String email, String passwordHash, String name, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Пользователь с email " + email + " уже существует");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setName(name);
        user.setRole(role);
        user.setActive(true);

        return userRepository.save(user);
    }

    public User updateUser(Long id, String name, String email, String phoneNumber, String bio, String avatarUrl) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (email != null && !email.equals(user.getEmail())) {
             if (userRepository.existsByEmail(email)) {
                 throw new RuntimeException("Email " + email + " уже занят");
             }
             user.setEmail(email);
        }

        user.setName(name);
        user.setPhoneNumber(phoneNumber);
        user.setBio(bio);
        user.setAvatarUrl(avatarUrl);

        return userRepository.save(user);
    }

    public User updateUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setActive(active);
        return userRepository.save(user);
    }

    public User updateUserRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setRole(role);
        return userRepository.save(user);
    }

    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        logger.info("Attempting to change password for user: {}", user.getEmail());
        logger.debug("Provided old password: '{}'", oldPassword);
        logger.debug("Stored password hash: '{}'", user.getPasswordHash());

        boolean matches = passwordEncoder.matches(oldPassword, user.getPasswordHash());
        logger.info("Password match result: {}", matches);

        if (!matches) {
            throw new RuntimeException("Неверный старый пароль for user ID: " + id);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password successfully changed for user: {}", user.getEmail());
    }

    public void adminChangePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        if (!registrationRepository.findByUser(user).isEmpty()) {
            throw new RuntimeException("Нельзя удалить пользователя с активными регистрациями. Заблокируйте его.");
        }
        
        userRepository.deleteById(id);
    }
}