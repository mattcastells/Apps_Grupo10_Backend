package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void requestOtp(String email) {
        // Enviar OTP por mail...
    }

    public String validateOtp(String email, String otp) {
        // Validar OTP con mongodb.
        // En caso de que este OK y el mail no tenga user, crear user.
        // En caso de OK y el mail tiene user, devolver ese user.
        // En caso de OTP no existente, devolver error.
        return null;
    }
}