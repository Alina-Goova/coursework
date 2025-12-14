package com.example.volunteersystem.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DatabaseMigration implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            jdbcTemplate.execute("ALTER TABLE registrations ADD COLUMN status VARCHAR(255) DEFAULT 'CONFIRMED'");
            System.out.println("Migration: Added 'status' column to 'registrations' table.");
        } catch (Exception e) {
            // System.out.println("Migration: Column 'status' already exists or other error: " + e.getMessage());
        }
        
        try {
            jdbcTemplate.execute("ALTER TABLE events ADD COLUMN max_participants INTEGER");
            System.out.println("Migration: Added 'max_participants' column to 'events' table.");
        } catch (Exception e) {
             // ignore
        }

        try {
            jdbcTemplate.execute("ALTER TABLE events ADD COLUMN current_participants INTEGER DEFAULT 0");
            System.out.println("Migration: Added 'current_participants' column to 'events' table.");
        } catch (Exception e) {
             // ignore
        }
        
        try {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN phone_number VARCHAR(255)");
        } catch (Exception e) {}
        
        try {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN bio TEXT");
        } catch (Exception e) {}
        
        try {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN avatar_url VARCHAR(255)");
        } catch (Exception e) {}

        try {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE"); 
            System.out.println("Migration: Added 'active' column to 'users' table.");
        } catch (Exception e) {
            // ignore if exists
        }

        try {
            jdbcTemplate.execute("ALTER TABLE events ADD COLUMN status VARCHAR(255) DEFAULT 'DRAFT'");
            System.out.println("Migration: Added 'status' column to 'events' table.");
        } catch (Exception e) {
             // ignore
        }

        try {
            jdbcTemplate.execute("ALTER TABLE events ADD COLUMN hidden BOOLEAN DEFAULT FALSE");
            System.out.println("Migration: Added 'hidden' column to 'events' table.");
        } catch (Exception e) {
             // ignore
        }
    }
}
