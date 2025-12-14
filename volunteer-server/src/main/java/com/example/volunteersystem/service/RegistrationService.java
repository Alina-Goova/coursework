package com.example.volunteersystem.service;

import com.example.volunteersystem.model.Registration;
import com.example.volunteersystem.model.User;
import com.example.volunteersystem.model.Event;
import com.example.volunteersystem.repository.RegistrationRepository;
import com.example.volunteersystem.repository.UserRepository;
import com.example.volunteersystem.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    public List<Registration> getRegistrationsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return registrationRepository.findByUser(user);
    }

    public List<Registration> getRegistrationsByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Мероприятие не найдено"));
        return registrationRepository.findByEvent(event);
    }

    public Registration registerForEvent(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Мероприятие не найдено"));

        if (event.getStartDate().isBefore(java.time.LocalDateTime.now())) {
             throw new RuntimeException("Нельзя зарегистрироваться на прошедшее мероприятие");
        }

        // Проверяем, не зарегистрирован ли уже пользователь
        if (registrationRepository.existsByUserAndEvent(user, event)) {
             Optional<Registration> existing = registrationRepository.findByUserAndEvent(user, event);
             if (existing.isPresent() && existing.get().getStatus() == com.example.volunteersystem.model.RegistrationStatus.CONFIRMED) {
                 throw new RuntimeException("Пользователь уже зарегистрирован на это мероприятие");
             } else if (existing.isPresent() && existing.get().getStatus() == com.example.volunteersystem.model.RegistrationStatus.CANCELLED) {
                 if (event.getMaxParticipants() != null && event.getMaxParticipants() > 0 &&
                     event.getCurrentParticipants() >= event.getMaxParticipants()) {
                     throw new RuntimeException("К сожалению, мест на мероприятие больше нет");
                 }

                 Registration reg = existing.get();
                 reg.setStatus(com.example.volunteersystem.model.RegistrationStatus.CONFIRMED);
                 reg.setRegistrationDate(java.time.LocalDateTime.now());
                 
                 event.setCurrentParticipants(event.getCurrentParticipants() + 1);
                 eventRepository.save(event);
                 
                 return registrationRepository.save(reg);
             }
        }

        if (event.getMaxParticipants() != null && event.getMaxParticipants() > 0 &&
            event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new RuntimeException("К сожалению, мест на мероприятие больше нет");
        }

        Registration registration = new Registration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setStatus(com.example.volunteersystem.model.RegistrationStatus.CONFIRMED);

        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        eventRepository.save(event);

        return registrationRepository.save(registration);
    }

    public void cancelRegistration(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Регистрация не найдена"));
        
        if (registration.getEvent().getStartDate().isBefore(java.time.LocalDateTime.now())) {
             throw new RuntimeException("Мероприятие уже началось, отмена невозможна");
        }
        
        if (registration.getStatus() == com.example.volunteersystem.model.RegistrationStatus.CONFIRMED) {
             Event event = registration.getEvent();
             if (event.getCurrentParticipants() > 0) {
                 event.setCurrentParticipants(event.getCurrentParticipants() - 1);
                 eventRepository.save(event);
             }
        }

        registration.setStatus(com.example.volunteersystem.model.RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);
    }

    public void cancelRegistrationByUserAndEvent(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Мероприятие не найдено"));

        Registration registration = registrationRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new RuntimeException("Регистрация не найдена"));

        if (event.getStartDate().isBefore(java.time.LocalDateTime.now())) {
             throw new RuntimeException("Мероприятие уже началось, отмена невозможна");
        }

        if (registration.getStatus() == com.example.volunteersystem.model.RegistrationStatus.CONFIRMED) {
             if (event.getCurrentParticipants() > 0) {
                 event.setCurrentParticipants(event.getCurrentParticipants() - 1);
                 eventRepository.save(event);
             }
        }

        registration.setStatus(com.example.volunteersystem.model.RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);
    }

    public void updateRegistrationStatus(Long registrationId, String statusStr) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Регистрация не найдена"));
        
        com.example.volunteersystem.model.RegistrationStatus oldStatus = registration.getStatus();
        com.example.volunteersystem.model.RegistrationStatus newStatus = com.example.volunteersystem.model.RegistrationStatus.valueOf(statusStr);
        
        if (oldStatus == newStatus) return;

        Event event = registration.getEvent();

        if (oldStatus == com.example.volunteersystem.model.RegistrationStatus.CONFIRMED &&
           (newStatus == com.example.volunteersystem.model.RegistrationStatus.CANCELLED || newStatus == com.example.volunteersystem.model.RegistrationStatus.REJECTED)) {
            if (event.getCurrentParticipants() > 0) {
                event.setCurrentParticipants(event.getCurrentParticipants() - 1);
                eventRepository.save(event);
            }
        } else if ((oldStatus == com.example.volunteersystem.model.RegistrationStatus.CANCELLED || oldStatus == com.example.volunteersystem.model.RegistrationStatus.REJECTED) &&
                   newStatus == com.example.volunteersystem.model.RegistrationStatus.CONFIRMED) {
            
            if (event.getMaxParticipants() != null && event.getMaxParticipants() > 0 && 
                event.getCurrentParticipants() >= event.getMaxParticipants()) {
                throw new RuntimeException("Нет свободных мест");
            }

            event.setCurrentParticipants(event.getCurrentParticipants() + 1);
            eventRepository.save(event);
        }

        registration.setStatus(newStatus);
        registrationRepository.save(registration);
    }
}