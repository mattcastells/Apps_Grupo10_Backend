package com.uade.ritmofitapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id private String id;
    @Indexed(unique = true)
    private String email;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastLogin;

    // User's full name
    private String name;

    // User's age
    private Integer age;

    // User's gender (e.g., "male", "female", "other")
    private String gender;

    // URL or base64 string for the user's profile picture
    private String profilePicture;

    // URL or base64 string for the user's profile picture
    private String rol;

    public User(String email) {
        this.email = email;
    }

}
