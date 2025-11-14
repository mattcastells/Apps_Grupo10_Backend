package com.uade.ritmofitapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "news")
public class News {
    @Id
    private String id;
    private String title;
    private String content;
    private String imageUrl;
    private LocalDateTime publishedDate;
    private LocalDateTime createdAt;
}
