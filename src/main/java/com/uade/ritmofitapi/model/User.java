package com.uade.ritmofitapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String email;
    private String password;
    private String name;
    private Integer age;
    private String gender;
    private String profilePicture;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastLogin;

    public User(String email) {
        this.email = email;
    }
}