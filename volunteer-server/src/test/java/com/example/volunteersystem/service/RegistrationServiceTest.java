package com.example.volunteersystem.service;

import com.example.volunteersystem.model.Event;
import com.example.volunteersystem.model.Registration;
import com.example.volunteersystem.model.RegistrationStatus;
import com.example.volunteersystem.model.User;
import com.example.volunteersystem.repository.EventRepository;
import com.example.volunteersystem.repository.RegistrationRepository;
import com.example.volunteersystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCancelRegistration_Success() {
        Long userId = 1L;
        Long eventId = 1L;
        
        User user = new User();
        user.setId(userId);
        
        Event event = new Event();
        event.setId(eventId);
        // Мероприятие через 48 часов
        event.setStartDate(LocalDateTime.now().plusHours(48));

        Registration registration = new Registration();
        registration.setId(1L);
        registration.setUser(user);
        registration.setEvent(event);
        registration.setStatus(RegistrationStatus.CONFIRMED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(registrationRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(registration));

        registrationService.cancelRegistrationByUserAndEvent(userId, eventId);

        assertEquals(RegistrationStatus.CANCELLED, registration.getStatus());
        verify(registrationRepository, times(1)).save(registration);
    }

    @Test
    void testCancelRegistration_TooLate() {
        Long userId = 1L;
        Long eventId = 1L;
        
        User user = new User();
        user.setId(userId);
        
        Event event = new Event();
        event.setId(eventId);
        // Мероприятие через 1 час (меньше 24)
        event.setStartDate(LocalDateTime.now().plusHours(1));

        Registration registration = new Registration();
        registration.setId(1L);
        registration.setUser(user);
        registration.setEvent(event);
        registration.setStatus(RegistrationStatus.CONFIRMED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(registrationRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(registration));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            registrationService.cancelRegistrationByUserAndEvent(userId, eventId);
        });

        assertTrue(exception.getMessage().contains("Отмена невозможна менее чем за 24 часа"));
        // Статус не должен измениться
        assertEquals(RegistrationStatus.CONFIRMED, registration.getStatus());
        // Сохранение не должно вызываться
        verify(registrationRepository, never()).save(registration);
    }
}
