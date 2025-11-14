package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.exception.InvalidCredentialsException;
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

        // Validar contraseña (mínimo 4 caracteres)
        if (password == null || password.length() < 4) {
            throw new RuntimeException("La contraseña debe tener al menos 4 caracteres.");
        }

        // Hasheamos la contraseña antes de guardarla
        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(name, email, hashedPassword, age, gender);
        userRepository.save(newUser);

        // Enviamos el OTP para la verificación del email
        String otp = otpService.sendOtpForVerification(email);
        log.info("OTP para {}: {}", email, otp);
        return newUser;
    }

    public String login(String email, String password) {
        log.info("🔐 Login attempt for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("❌ User not found: {}", email);
                    return new InvalidCredentialsException("Usuario o contraseña inválidos.");
                });

        log.info("✅ User found: {} (verified: {})", email, user.isVerified());

        // Verificar que el email esté verificado
        if (!user.isVerified()) {
            log.warn("⚠️ User not verified: {}", email);
            throw new InvalidCredentialsException("Por favor, verifica tu email antes de iniciar sesión.");
        }

        // Comparamos la contraseña enviada con el hash almacenado
        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        log.info("🔑 Password match result for {}: {}", email, passwordMatches);

        if (!passwordMatches) {
            log.warn("❌ Invalid password for user: {}", email);
            throw new InvalidCredentialsException("Usuario o contraseña inválidos.");
        }

        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);

        log.info("✅ Login successful for: {}", email);
        return jwtService.generateToken(user.getId());
    }


    public String verifyEmail(String email, String otp) {
        otpService.validateOtp(email, otp);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        user.setVerified(true);
        userRepository.save(user);

        // Generamos y retornamos un JWT token para que el usuario quede autenticado inmediatamente
        return jwtService.generateToken(user.getId());
    }

    // --- Forgot Password: Enviar OTP para recuperación de contraseña ---
    public void forgotPassword(String email) {
        // Verificamos que el usuario existe
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ese email."));

        // Enviamos el OTP usando el mismo servicio que en registro
        String otp = otpService.sendOtpForVerification(email);
        log.info("OTP de recuperación para {}: {}", email, otp);
    }

    // --- Verify Reset OTP: Validar OTP sin eliminarlo ---
    public void verifyResetOtp(String email, String otp) {
        // Validamos el OTP pero NO lo eliminamos (se necesita para el siguiente paso)
        otpService.validateOtpWithoutDeleting(email, otp);
    }

    // --- Reset Password: Cambiar contraseña con OTP ---
    public void resetPassword(String email, String otp, String newPassword) {
        // Validamos el OTP y lo eliminamos
        otpService.validateOtp(email, otp);

        // Validar la nueva contraseña (mínimo 4 caracteres)
        if (newPassword == null || newPassword.length() < 4) {
            throw new RuntimeException("La contraseña debe tener al menos 4 caracteres.");
        }

        // Buscamos el usuario
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // Hasheamos la nueva contraseña con BCrypt (SEGURIDAD)
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);

        // Guardamos el usuario con la nueva contraseña
        userRepository.save(user);
        log.info("Contraseña actualizada exitosamente para: {}", email);
    }
}