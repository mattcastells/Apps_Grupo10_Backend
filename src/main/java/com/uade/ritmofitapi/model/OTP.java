package com.uade.ritmofitapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document(collection = "user_otp")
public class OTP {
    @Indexed(unique = true)
    private String email;
    private String code;
    
}
