package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.request.UserCreationRequest;
import com.uade.ritmofitapi.dto.request.UserRequest;
import com.uade.ritmofitapi.dto.request.UpdatePhotoRequest;
import com.uade.ritmofitapi.dto.response.UserResponse;
import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) { 
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Mantengo el create original por compatibilidad con otros flujos existentes
    public User createUser(UserCreationRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new DuplicateKeyException("El correo electrónico ya está en uso.");
        });

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // (pendiente: hashear en futuro)
        user.setAge(request.getAge());
        user.setGender(request.getGender());

        return userRepository.save(user);
    }

    // --- Perfil: obtener por id (lo que consume el front) ---
    public UserResponse getById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return toResponse(user);
    }

    // --- Perfil: actualizar por id (usa shape de UserRequest del front) ---
    public void updateById(String userId, UserRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (req.getEmail() != null && !req.getEmail().isBlank()
                && !req.getEmail().equalsIgnoreCase(user.getEmail())) {
            userRepository.findByEmail(req.getEmail()).ifPresent(u -> {
                throw new DuplicateKeyException("Email ya en uso");
            });
            user.setEmail(req.getEmail());
        }
        if (req.getName() != null)        user.setName(req.getName());
        if (req.getAge() != null)         user.setAge(req.getAge());
        if (req.getGender() != null)      user.setGender(req.getGender());
        if (req.getProfilePicture() != null) user.setProfilePicture(req.getProfilePicture());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        userRepository.save(user);
    }

    // --- Perfil: actualizar foto (body: { "photoUrl": "..." }) ---
    public void updatePhoto(String userId, UpdatePhotoRequest req) {
        if (req.getPhotoUrl() == null || req.getPhotoUrl().isBlank()) {
            throw new IllegalArgumentException("photoUrl es requerido");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setProfilePicture(req.getPhotoUrl());
        userRepository.save(user);
    }

    private UserResponse toResponse(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setEmail(u.getEmail());
        r.setName(u.getName());
        r.setAge(u.getAge());
        r.setGender(u.getGender());
        r.setProfilePicture(u.getProfilePicture());
        // r.setPassword(null); // por seguridad lo dejamos null/omitido
        return r;
    }
}