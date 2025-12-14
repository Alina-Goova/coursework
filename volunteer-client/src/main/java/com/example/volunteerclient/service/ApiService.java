package com.example.volunteerclient.service;

import com.example.volunteerclient.model.AuthResponse;
import com.example.volunteerclient.model.Event;
import com.example.volunteerclient.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ApiService {
    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String authToken;

    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public CompletableFuture<AuthResponse> login(String email, String password) {
        try {
            String requestBody = objectMapper.writeValueAsString(new LoginRequest(email, password));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                return objectMapper.readValue(response.body(), AuthResponse.class);
                            } catch (Exception e) {
                                throw new RuntimeException("Ошибка парсинга ответа: " + e.getMessage());
                            }
                        } else {
                            throw new RuntimeException("Ошибка авторизации: " + response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<AuthResponse> register(String email, String password, String name, String role) {
        try {
            String requestBody = objectMapper.writeValueAsString(new RegisterRequest(email, password, name, role));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                return objectMapper.readValue(response.body(), AuthResponse.class);
                            } catch (Exception e) {
                                throw new RuntimeException("Ошибка парсинга ответа: " + e.getMessage());
                            }
                        } else {
                            throw new RuntimeException(response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<List<Event>> getEvents() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/events"))
                .GET();

        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, Event.class));
                        } catch (Exception e) {
                            throw new RuntimeException("Ошибка парсинга мероприятий: " + e.getMessage());
                        }
                    } else {
                        throw new RuntimeException("Ошибка получения мероприятий: " + response.body());
                    }
                });
    }

    public CompletableFuture<Map<String, Long>> getStatistics() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/events/statistics"))
                .GET();

        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Long>>(){});
                        } catch (Exception e) {
                            throw new RuntimeException("Ошибка парсинга статистики: " + e.getMessage());
                        }
                    } else {
                        throw new RuntimeException("Ошибка получения статистики: " + response.body());
                    }
                });
    }

    public CompletableFuture<List<Event>> searchEvents(String title) {
        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/events/search?title=" + encodedTitle))
                .GET();

        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, Event.class));
                        } catch (Exception e) {
                            throw new RuntimeException("Ошибка парсинга мероприятий: " + e.getMessage());
                        }
                    } else {
                        throw new RuntimeException("Ошибка поиска мероприятий: " + response.body());
                    }
                });
    }

    public CompletableFuture<Event> createEvent(Event event) {
        try {
            String requestBody = objectMapper.writeValueAsString(event);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/events"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                return objectMapper.readValue(response.body(), Event.class);
                            } catch (Exception e) {
                                throw new RuntimeException("Ошибка парсинга ответа: " + e.getMessage());
                            }
                        } else {
                            throw new RuntimeException("Ошибка создания мероприятия: " + response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
    public CompletableFuture<Void> cancelEvent(Long eventId) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/events/" + eventId + "/cancel"))
                .POST(HttpRequest.BodyPublishers.noBody());

        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return null;
                    } else {
                        throw new RuntimeException("Ошибка отмены мероприятия: " + response.body());
                    }
                });
    }

    public CompletableFuture<Void> deleteEvent(Long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/events/" + id))
                .header("Authorization", "Bearer " + authToken)
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return null;
                    } else {
                        throw new RuntimeException("Ошибка удаления мероприятия: " + response.body());
                    }
                });
    }

    public CompletableFuture<List<Event>> getUserEvents(Long userId) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/" + userId + "/events"))
                .GET();

        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, Event.class));
                        } catch (Exception e) {
                            throw new RuntimeException("Ошибка парсинга мероприятий пользователя: " + e.getMessage());
                        }
                    } else {
                        throw new RuntimeException("Ошибка получения мероприятий пользователя: " + response.body());
                    }
                });
    }

    public CompletableFuture<List<com.example.volunteerclient.model.Registration>> getUserRegistrations(Long userId) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/registrations/user/" + userId))
                .GET();

        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, com.example.volunteerclient.model.Registration.class));
                        } catch (Exception e) {
                            throw new RuntimeException("Ошибка парсинга регистраций: " + e.getMessage());
                        }
                    } else {
                        throw new RuntimeException("Ошибка получения регистраций: " + response.body());
                    }
                });
    }

    public CompletableFuture<List<com.example.volunteerclient.model.Registration>> getAllRegistrations() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/registrations/all"))
                .GET();

        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, com.example.volunteerclient.model.Registration.class));
                        } catch (Exception e) {
                            throw new RuntimeException("Ошибка парсинга регистраций: " + e.getMessage());
                        }
                    } else {
                        throw new RuntimeException("Ошибка получения всех регистраций: " + response.body());
                    }
                });
    }

    public CompletableFuture<Void> updateRegistrationStatus(Long registrationId, String status) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of("status", status));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/registrations/" + registrationId + "/status"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            return null;
                        } else {
                            throw new RuntimeException("Ошибка обновления статуса регистрации: " + response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<Void> cancelRegistration(Long userId, Long eventId) {
        try {
            String requestBody = objectMapper.writeValueAsString(new RegistrationRequest(userId, eventId));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/registrations/cancel"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            return null;
                        } else {
                            throw new RuntimeException("Ошибка отмены регистрации: " + response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<User> updateProfile(Long userId, String name, String email, String phoneNumber, String bio, String avatarUrl) {
        try {
            String requestBody = objectMapper.writeValueAsString(new UpdateProfileRequest(name, email, phoneNumber, bio, avatarUrl));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/" + userId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                return objectMapper.readValue(response.body(), User.class);
                            } catch (Exception e) {
                                throw new RuntimeException("Ошибка парсинга ответа: " + e.getMessage());
                            }
                        } else {
                            throw new RuntimeException("Ошибка обновления профиля: " + response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            String requestBody = objectMapper.writeValueAsString(new ChangePasswordRequest(oldPassword, newPassword));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/" + userId + "/change-password"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            return null;
                        } else {
                            throw new RuntimeException("Ошибка смены пароля: " + response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }


    public CompletableFuture<List<User>> getAllUsers() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users"))
                .GET();

        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
                        } catch (Exception e) {
                            throw new RuntimeException("Ошибка парсинга пользователей: " + e.getMessage());
                        }
                    } else {
                        throw new RuntimeException("Ошибка получения пользователей: " + response.body());
                    }
                });
    }

    public CompletableFuture<User> createUser(String email, String password, String name, String role) {
        try {
            // Note: passwordHash in UserCreateRequest on server expects the raw password here as we decided
            String requestBody = objectMapper.writeValueAsString(new UserCreateRequest(email, password, name, role));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                return objectMapper.readValue(response.body(), User.class);
                            } catch (Exception e) {
                                throw new RuntimeException("Ошибка парсинга ответа: " + e.getMessage());
                            }
                        } else {
                            throw new RuntimeException(response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<User> updateUserStatus(Long userId, boolean active) {
        try {
            String requestBody = objectMapper.writeValueAsString(new UserStatusRequest(active));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/" + userId + "/status"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                return objectMapper.readValue(response.body(), User.class);
                            } catch (Exception e) {
                                throw new RuntimeException("Ошибка парсинга ответа: " + e.getMessage());
                            }
                        } else {
                            throw new RuntimeException("Ошибка обновления статуса: " + response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<User> updateUserRole(Long userId, String role) {
        try {
            String requestBody = objectMapper.writeValueAsString(new UserRoleRequest(role));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/" + userId + "/role"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                return objectMapper.readValue(response.body(), User.class);
                            } catch (Exception e) {
                                throw new RuntimeException("Ошибка парсинга ответа: " + e.getMessage());
                            }
                        } else {
                            throw new RuntimeException("Ошибка обновления роли: " + response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<Void> adminChangePassword(Long userId, String newPassword) {
        try {
            String requestBody = objectMapper.writeValueAsString(new AdminChangePasswordRequest(newPassword));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/" + userId + "/admin-change-password"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            return null;
                        } else {
                            throw new RuntimeException("Ошибка смены пароля: " + response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<Void> deleteUser(Long userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/" + userId))
                .header("Authorization", "Bearer " + authToken)
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return null;
                    } else {
                        throw new RuntimeException("Ошибка удаления пользователя: " + response.body());
                    }
                });
    }

    public CompletableFuture<Event> updateEvent(Event event) {
        try {
            String requestBody = objectMapper.writeValueAsString(event);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/events/" + event.getId()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                return objectMapper.readValue(response.body(), Event.class);
                            } catch (Exception e) {
                                throw new RuntimeException("Ошибка парсинга ответа: " + e.getMessage());
                            }
                        } else {
                            throw new RuntimeException("Ошибка обновления мероприятия: " + response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<Void> registerForEvent(Long userId, Long eventId) {
        try {
            String requestBody = objectMapper.writeValueAsString(new RegistrationRequest(userId, eventId));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/registrations"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            return null;
                        } else {
                            throw new RuntimeException("Ошибка регистрации: " + response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }


    private static class UserCreateRequest {
        public String email;
        public String passwordHash;
        public String name;
        public String role;

        public UserCreateRequest(String email, String password, String name, String role) {
            this.email = email;
            this.passwordHash = password;
            this.name = name;
            this.role = role;
        }
    }

    private static class UserStatusRequest {
        public boolean active;
        public UserStatusRequest(boolean active) { this.active = active; }
    }

    private static class UserRoleRequest {
        public String role;
        public UserRoleRequest(String role) { this.role = role; }
    }

    private static class AdminChangePasswordRequest {
        public String newPassword;
        public AdminChangePasswordRequest(String newPassword) { this.newPassword = newPassword; }
    }

    private static class UpdateProfileRequest {
        public String name;
        public String email;
        public String phoneNumber;
        public String bio;
        public String avatarUrl;

        public UpdateProfileRequest(String name, String email, String phoneNumber, String bio, String avatarUrl) {
            this.name = name;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.bio = bio;
            this.avatarUrl = avatarUrl;
        }
    }

    private static class ChangePasswordRequest {
        public String oldPassword;
        public String newPassword;

        public ChangePasswordRequest(String oldPassword, String newPassword) {
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
        }
    }

    private static class RegistrationRequest {
        public Long userId;
        public Long eventId;

        public RegistrationRequest(Long userId, Long eventId) {
            this.userId = userId;
            this.eventId = eventId;
        }
    }

    private static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    private static class RegisterRequest {
        public String email;
        public String password;
        public String name;
        public String role;

        public RegisterRequest(String email, String password, String name, String role) {
            this.email = email;
            this.password = password;
            this.name = name;
            this.role = role;
        }
    }
}