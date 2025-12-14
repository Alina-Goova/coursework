package com.example.volunteersystem.service;

import com.example.volunteersystem.model.Event;
import com.example.volunteersystem.model.EventStatus;
import com.example.volunteersystem.model.User;
import com.example.volunteersystem.repository.EventRepository;
import com.example.volunteersystem.repository.RegistrationRepository;
import com.example.volunteersystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getEventsOrderedByDate() {
        return eventRepository.findByOrderByStartDateAsc();
    }

    public List<Event> searchEventsByTitle(String title) {
        return eventRepository.findByTitleContainingIgnoreCase(title);
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public Event createEvent(String title, String description, String location,
                             LocalDateTime startDate, LocalDateTime endDate, Integer maxParticipants,
                             EventStatus status, String category, Long organizerId, boolean hidden) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setLocation(location);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setMaxParticipants(maxParticipants);
        event.setStatus(status != null ? status : EventStatus.DRAFT);
        event.setCategory(category);
        event.setHidden(hidden);

        if (organizerId != null) {
            User organizer = userRepository.findById(organizerId)
                    .orElseThrow(() -> new RuntimeException("Организатор не найден"));
            event.setOrganizer(organizer);
        }

        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, String title, String description, String location,
                             LocalDateTime startDate, LocalDateTime endDate, Integer maxParticipants,
                             EventStatus status, String category, boolean hidden) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Мероприятие не найдено"));

        if (maxParticipants != null && maxParticipants < event.getCurrentParticipants()) {
             throw new RuntimeException("Нельзя уменьшить количество участников ниже текущего (" + event.getCurrentParticipants() + ")");
        }

        boolean datesChanged = !event.getStartDate().isEqual(startDate) || !event.getEndDate().isEqual(endDate);
        if (datesChanged) {
            long hoursUntilStart = ChronoUnit.HOURS.between(LocalDateTime.now(), event.getStartDate());
            if (hoursUntilStart < 24 && hoursUntilStart >= 0) {
                 throw new RuntimeException("Нельзя менять дату мероприятия менее чем за 24 часа до начала");
            }
        }

        event.setTitle(title);
        event.setDescription(description);
        event.setLocation(location);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setMaxParticipants(maxParticipants);
        if (status != null) event.setStatus(status);
        if (category != null) event.setCategory(category);
        event.setHidden(hidden);

        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Мероприятие не найдено"));

        if (event.getCurrentParticipants() > 0) {
             throw new RuntimeException("Нельзя удалить мероприятие с участниками. Используйте 'Отмену'.");
        }
        
        eventRepository.deleteById(id);
    }
    
    public void cancelEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Мероприятие не найдено"));
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    public Map<String, Long> getStatistics() {
        List<Event> all = eventRepository.findAll();
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) all.size());
        stats.put("active", all.stream().filter(e -> e.getStatus() == EventStatus.ACTIVE).count());
        stats.put("planned", all.stream().filter(e -> e.getStatus() == EventStatus.PLANNED).count());
        stats.put("completed", all.stream().filter(e -> e.getStatus() == EventStatus.COMPLETED).count());
        return stats;
    }
}