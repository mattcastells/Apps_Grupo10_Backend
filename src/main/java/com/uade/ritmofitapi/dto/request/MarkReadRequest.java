package com.uade.ritmofitapi.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class MarkReadRequest {
    private List<String> notificationIds;
}

