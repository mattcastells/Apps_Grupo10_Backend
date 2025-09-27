package com.uade.ritmofitapi.dto.request;

import lombok.Data;

@Data
public class UserRequest {
    private String email;
    private String name;
    private Integer age;
    private String gender;
    private String profilePicture; // opcional
    private String password;       // opcional para update
}