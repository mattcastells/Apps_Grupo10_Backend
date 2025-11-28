package com.uade.ritmofitapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.uade.ritmofitapi.model.Notification;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String type;
    private String title;
    private String body;
    private boolean read;
    private LocalDateTime createdAt;
    private String bookingId;
    private String scheduledClassId;
    private String actionUrl;
    private LocalDateTime oldDateTime;
    private LocalDateTime newDateTime;

    public static NotificationResponse fromEntity(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType().name());
        response.setTitle(notification.getTitle());
        response.setBody(notification.getBody());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());
        response.setBookingId(notification.getBookingId());
        response.setScheduledClassId(notification.getScheduledClassId());
        response.setActionUrl(notification.getActionUrl());
        response.setOldDateTime(notification.getOldDateTime());
        response.setNewDateTime(notification.getNewDateTime());
        return response;
    }
}

