package com.example.volunteersystem.controller;

import com.example.volunteersystem.model.Event;
import com.example.volunteersystem.model.EventStatus;
import com.example.volunteersystem.dto.EventDTO;
import com.example.volunteersystem.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:8081")
public class EventController {

    @Autowired
    private EventService eventService;

    private EventDTO convertToDTO(Event event) {
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

    @GetMapping
    public List<EventDTO> getAllEvents() {
        return eventService.getAllEvents().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/ordered")
    public List<EventDTO> getEventsOrderedByDate() {
        return eventService.getEventsOrderedByDate().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<EventDTO> searchEvents(@RequestParam String title) {
        return eventService.searchEventsByTitle(title).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/statistics")
    public Map<String, Long> getStatistics() {
        return eventService.getStatistics();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(event -> ResponseEntity.ok(convertToDTO(event)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public EventDTO createEvent(@RequestBody EventCreateRequest request) {
        Event event = eventService.createEvent(
                request.getTitle(),
                request.getDescription(),
                request.getLocation(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMaxParticipants(),
                request.getStatus(),
                request.getCategory(),
                request.getOrganizerId(),
                request.isHidden()
        );
        return convertToDTO(event);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody EventCreateRequest request) {
        try {
            Event event = eventService.updateEvent(
                    id,
                    request.getTitle(),
                    request.getDescription(),
                    request.getLocation(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getMaxParticipants(),
                    request.getStatus(),
                    request.getCategory(),
                    request.isHidden()
            );
            return ResponseEntity.ok(convertToDTO(event));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelEvent(@PathVariable Long id) {
        try {
            eventService.cancelEvent(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventCreateRequest {
        private String title;
        private String description;
        private String location;
        private java.time.LocalDateTime startDate;
        private java.time.LocalDateTime endDate;
        private Integer maxParticipants;
        private EventStatus status;
        private String category;
        private Long organizerId;
        private boolean hidden;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public java.time.LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(java.time.LocalDateTime startDate) { this.startDate = startDate; }

        public java.time.LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(java.time.LocalDateTime endDate) { this.endDate = endDate; }

        public Integer getMaxParticipants() { return maxParticipants; }
        public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
        
        public EventStatus getStatus() { return status; }
        public void setStatus(EventStatus status) { this.status = status; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public Long getOrganizerId() { return organizerId; }
        public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }
        
        public boolean isHidden() { return hidden; }
        public void setHidden(boolean hidden) { this.hidden = hidden; }
    }
}