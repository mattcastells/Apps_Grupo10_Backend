package com.uade.ritmofitapi.dto;

import com.uade.ritmofitapi.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {
    private Notification.NotificationType type;
    private String title;
    private String message;
    private LocalDateTime scheduledFor;
    private String bookingId;
    private String scheduledClassId;
}
