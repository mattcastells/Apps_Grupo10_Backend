package com.uade.ritmofitapi.dto.response;

import lombok.Data;

@Data
public class HistoryDetailResponse {
    private String id;
    private String discipline;
    private String teacher;
    private String site;
    private String location;
    private String startDateTime; // ISO-8601 format
    private int durationMinutes;
    private String attendanceStatus; // "CONFIRMADA", "CANCELADA", etc.
    private Review userReview; // null si no hay

    @Data
    public static class Review {
        private Integer rating; // 1..5
        private String comment; // null si solo rating
        private String createdAt; // opcional
    }
}
