package com.uade.ritmofitapi.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RatingResponse {
    private String id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
