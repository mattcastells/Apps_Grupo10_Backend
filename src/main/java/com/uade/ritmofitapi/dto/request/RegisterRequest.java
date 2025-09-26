package com.uade.ritmofitapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "Debe ser una dirección de email válida")
    String email;

    @NotBlank(message = "El password no puede estar vacío")
    String password;

    @NotBlank(message = "El nombre no puede estar vacío")
    String name;

    @NotBlank(message = "La edad no puede estar vacía")
    Integer age;

    @NotBlank(message = "El genero no puede estar vacío")
    String gender;

}
