package com.example.volunteersystem.dto;

import com.example.volunteersystem.model.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String email;
    private Role role;
    private String name;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String bio;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("registration_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDate;

    private boolean active;

    public UserDTO() {}

    public UserDTO(Long id, String email, Role role, String name, String phoneNumber, String bio, String avatarUrl, LocalDateTime registrationDate, boolean active) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.registrationDate = registrationDate;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}