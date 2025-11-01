package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService; // Refactorizamos la lógica de OTP a su propio servicio

    public AuthService(
            UserRepository userRepository,
            OtpService otpService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(String name, String email, String password, Integer age, String gender) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("El email ya está en uso.");
        }
        // Hasheamos la contraseña antes de guardarla
        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(name, email, hashedPassword, age, gender);
        userRepository.save(newUser);

        // Enviamos el OTP para la verificación del email
        otpService.sendOtpForVerification(email);
        return newUser;
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña inválidos."));

        //if (!user.isVerified()) {
        //    throw new RuntimeException("Por favor, verifica tu email antes de iniciar sesión.");
        //}

        // Comparamos la contraseña enviada con el hash almacenado
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Usuario o contraseña inválidos.");
        }

        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);

        return jwtService.generateToken(user.getId());
    }


    public void verifyEmail(String email, String otp) {
        otpService.validateOtp(email, otp);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        user.setVerified(true);
        userRepository.save(user);
    }
}