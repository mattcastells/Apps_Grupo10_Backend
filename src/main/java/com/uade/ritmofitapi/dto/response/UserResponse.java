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
    private String role;
    private String password;
}