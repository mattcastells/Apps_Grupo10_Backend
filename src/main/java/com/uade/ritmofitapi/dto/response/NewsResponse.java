package com.uade.ritmofitapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponse {
    private String id;
    private String title;
    private String content;
    private String image;
    private String date;
}
