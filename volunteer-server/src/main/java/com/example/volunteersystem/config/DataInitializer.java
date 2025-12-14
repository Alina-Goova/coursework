package com.example.volunteersystem.config;

import com.example.volunteersystem.model.Role;
import com.example.volunteersystem.model.User;
import com.example.volunteersystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@volunteer.org").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@volunteer.org");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setName("Администратор");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Создан администратор по умолчанию: admin@volunteer.org / admin123");
        }

        if (userRepository.findByEmail("volunteer@test.org").isEmpty()) {
            User volunteer = new User();
            volunteer.setEmail("volunteer@test.org");
            volunteer.setPasswordHash(passwordEncoder.encode("volunteer123"));
            volunteer.setName("Тестовый Волонтер");
            volunteer.setRole(Role.VOLUNTEER);
            userRepository.save(volunteer);
            System.out.println("Создан тестовый волонтер: volunteer@test.org / volunteer123");
        }
    }
}