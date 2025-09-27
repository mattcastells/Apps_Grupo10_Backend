package com.uade.ritmofitapi.dto.response;

import lombok.Data;

@Data
public class HistoryItemResponse {
    private String id;
    private String discipline;
    private String teacher;
    private String site;
    private String location;
    private String startDateTime; // ISO-8601 format
    private int durationMinutes;
}
