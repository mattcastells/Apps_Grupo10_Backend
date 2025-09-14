package com.uade.ritmofitapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "Debe ser una dirección de email válida")
    String email;
}
