package com.example.volunteersystem.controller;

import com.example.volunteersystem.dto.RegistrationDTO;
import com.example.volunteersystem.model.Registration;
import com.example.volunteersystem.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "http://localhost:8081")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    private RegistrationDTO convertToDTO(Registration registration) {
        String status = registration.getStatus() != null ? registration.getStatus().name() : "CONFIRMED";
        return new RegistrationDTO(
                registration.getId(),
                registration.getUser().getId(),
                registration.getUser().getName(),
                registration.getEvent().getId(),
                registration.getEvent().getTitle(),
                registration.getEvent().getStartDate(),
                registration.getEvent().getEndDate(),
                registration.getRegistrationDate(),
                status
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RegistrationDTO>> getUserRegistrations(@PathVariable Long userId) {
        List<RegistrationDTO> registrations = registrationService.getRegistrationsByUser(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/all")
    public ResponseEntity<List<RegistrationDTO>> getAllRegistrations() {
        List<RegistrationDTO> registrations = registrationService.getAllRegistrations().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(registrations);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateRegistrationStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        try {
            String newStatus = statusUpdate.get("status");
            registrationService.updateRegistrationStatus(id, newStatus);
            return ResponseEntity.ok(Map.of("message", "Статус обновлен"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> registerUserForEvent(@RequestBody RegistrationRequest request) {
        try {
            registrationService.registerForEvent(request.getUserId(), request.getEventId());
            return ResponseEntity.ok(Map.of("message", "Успешная регистрация"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelRegistration(@RequestBody RegistrationRequest request) {
         try {
            registrationService.cancelRegistrationByUserAndEvent(request.getUserId(), request.getEventId());
            return ResponseEntity.ok(Map.of("message", "Регистрация отменена"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public static class RegistrationRequest {
        private Long userId;
        private Long eventId;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getEventId() { return eventId; }
        public void setEventId(Long eventId) { this.eventId = eventId; }
    }
}