package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.request.UserCreationRequest;
import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    // Inyectar un PasswordEncoder para hashear contraseñas en un caso real
    // private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(UserCreationRequest request) {
        // Aquí deberías validar si el email ya existe, por ejemplo.
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El correo electrónico ya está en uso.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // Temporalmente sin hashear
        user.setAge(request.getAge());
        user.setGender(request.getGender());

        return userRepository.save(user);
    }
}