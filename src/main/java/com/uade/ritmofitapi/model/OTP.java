package com.uade.ritmofitapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_otp")
public class OTP {
    @Id
    private String email;
    private String code;
    private LocalDate createdAt;

    public OTP(String email, String code) {
        this.email = email;
        this.code = code;
        this.createdAt = LocalDate.now();
    }

}
