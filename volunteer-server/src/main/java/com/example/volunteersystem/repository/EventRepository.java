package com.example.volunteersystem.repository;

import com.example.volunteersystem.model.Event;
import com.example.volunteersystem.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findByTitleContainingIgnoreCase(String title);
    List<Event> findByOrderByStartDateAsc();
    
    List<Event> findByStatus(EventStatus status);
    List<Event> findByCategory(String category);
    List<Event> findByOrganizerId(Long organizerId);
    List<Event> findByHiddenFalse();
}