package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;

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
        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(name, email, hashedPassword, age, gender);
        
        String otp = otpService.generateOtp();
        newUser.setOtp(otp);
        newUser.setOtpExpires(LocalDateTime.now().plusMinutes(15));
        
        userRepository.save(newUser);
        
        otpService.sendOtpEmail(email, otp);
        log.info("OTP para {}: {}", email, otp);
        return newUser;
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña inválidos."));

        if (!user.isVerified()) {
            throw new RuntimeException("Por favor, verifica tu email antes de iniciar sesión.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Usuario o contraseña inválidos.");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return jwtService.generateToken(user.getId());
    }

    public void verifyEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new RuntimeException("OTP inválido.");
        }

        if (user.getOtpExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El OTP ha expirado.");
        }

        user.setVerified(true);
        user.setOtp(null);
        user.setOtpExpires(null);
        userRepository.save(user);
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        String otp = otpService.generateOtp();
        user.setPasswordResetOtp(otp);
        user.setPasswordResetOtpExpires(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        otpService.sendPasswordResetEmail(email, otp);
        log.info("Password reset OTP para {}: {}", email, otp);
    }

    public void resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (user.getPasswordResetOtp() == null || !user.getPasswordResetOtp().equals(otp)) {
            throw new RuntimeException("OTP de reseteo de contraseña inválido.");
        }

        if (user.getPasswordResetOtpExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El OTP de reseteo de contraseña ha expirado.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetOtp(null);
        user.setPasswordResetOtpExpires(null);
        userRepository.save(user);
    }
}
