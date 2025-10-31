package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.request.UpdatePhotoRequest;
import com.uade.ritmofitapi.dto.request.UserCreationRequest;
import com.uade.ritmofitapi.dto.request.UserRequest;
import com.uade.ritmofitapi.dto.response.UserResponse;
import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) { this.userService = userService; }

    // POST /users (se mantiene por compatibilidad)
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserCreationRequest request) {
        try {
            User createdUser = userService.createUser(request);
            return ResponseEntity.ok(createdUser);
        } catch (DuplicateKeyException dup) {
            return ResponseEntity.status(409).body("Email ya en uso");
        }
    }

    // GET /users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        try {
            UserResponse resp = userService.getById(id);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    // PUT /users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserRequest req) {
        try {
            userService.updateById(id, req);
            UserResponse updatedUser = userService.getById(id);
            return ResponseEntity.ok(updatedUser);
        } catch (DuplicateKeyException dup) {
            return ResponseEntity.status(409).body("Email ya en uso");
        } catch (IllegalArgumentException bad) {
            return ResponseEntity.badRequest().body(bad.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    // PUT /users/{id}/photo
    @PutMapping("/{id}/photo")
    public ResponseEntity<?> updatePhoto(@PathVariable String id, @RequestBody UpdatePhotoRequest req) {
        try {
            userService.updatePhoto(id, req);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException bad) {
            return ResponseEntity.badRequest().body(bad.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }
}