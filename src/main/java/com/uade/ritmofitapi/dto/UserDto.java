package com.uade.ritmofitapi.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserDto {
    private String id;
    private String email;
    private String name;
    private Integer age;
    private String gender;
    private String profilePicture;
    private String role;
    // Opcional: puedes agregar más campos si lo necesitas
}
