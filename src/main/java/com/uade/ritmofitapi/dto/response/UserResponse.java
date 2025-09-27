package com.uade.ritmofitapi.dto.response;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String name;
    private Integer age;
    private String gender;
    private String profilePicture;
    // Campo presente en el front; lo dejamos nulo para no exponer secretos
    private String password;
}