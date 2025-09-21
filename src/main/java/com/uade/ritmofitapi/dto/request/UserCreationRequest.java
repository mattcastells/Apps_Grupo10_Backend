package com.uade.ritmofitapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreationRequest {
    private String email;
    private String password;
    private String name;
    private Integer age;
    private String gender;
}
