package com.example.volunteersystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class RegistrationDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long eventId;
    private String eventTitle;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime eventStartDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime eventEndDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDate;

    private String status;

    public RegistrationDTO() {}

    public RegistrationDTO(Long id, Long userId, String userName, Long eventId,
                           String eventTitle, LocalDateTime eventStartDate, LocalDateTime eventEndDate,
                           LocalDateTime registrationDate, String status) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.registrationDate = registrationDate;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public LocalDateTime getEventStartDate() { return eventStartDate; }
    public void setEventStartDate(LocalDateTime eventStartDate) { this.eventStartDate = eventStartDate; }

    public LocalDateTime getEventEndDate() { return eventEndDate; }
    public void setEventEndDate(LocalDateTime eventEndDate) { this.eventEndDate = eventEndDate; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}