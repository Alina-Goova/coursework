package com.example.volunteerclient.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private Long id;
    private String email;
    private String role;
    private String name;

    @JsonProperty("registration_date")
    private String registrationDate;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String bio;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private boolean active = true;

    public User() {}

    public User(Long id, String email, String role, String name, String phoneNumber, String bio, String avatarUrl, String registrationDate, boolean active) {
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

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}