package com.uade.ritmofitapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VerifyResetOtpRequest {
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "Debe ser una dirección de email válida")
    private String email;

    @NotBlank(message = "El OTP no puede estar vacío")
    private String otp;
}
