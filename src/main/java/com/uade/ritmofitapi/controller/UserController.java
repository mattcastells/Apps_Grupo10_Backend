package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.request.UpdatePhotoRequest;
import com.uade.ritmofitapi.dto.request.UserCreationRequest;
import com.uade.ritmofitapi.dto.request.UserRequest;
import com.uade.ritmofitapi.dto.response.UserResponse;
import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserCreationRequest request) {
        try {
            User createdUser = userService.createUser(request);
            return ResponseEntity.ok(createdUser);
        } catch (DuplicateKeyException dup) {
            return ResponseEntity.status(409).body("Email ya en uso");
        }
    }

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
            return ResponseEntity.ok().build();
        } catch (DuplicateKeyException dup) {
            return ResponseEntity.status(409).body("Email ya en uso");
        } catch (IllegalArgumentException bad) {
            return ResponseEntity.badRequest().body(bad.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    @PutMapping("/my-photo")
    public ResponseEntity<?> updatePhoto(Authentication authentication, @RequestBody UpdatePhotoRequest req) {
        User user = (User) authentication.getPrincipal();
        try {
            userService.updatePhoto(user.getId(), req);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException bad) {
            return ResponseEntity.badRequest().body(bad.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }
}
