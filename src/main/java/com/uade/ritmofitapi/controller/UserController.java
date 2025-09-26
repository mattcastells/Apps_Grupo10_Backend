package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.model.Professor;
import com.uade.ritmofitapi.model.Student;
import com.uade.ritmofitapi.repository.UserRepository;
import com.uade.ritmofitapi.dto.UserDto;
import com.uade.ritmofitapi.dto.ProfessorDto;
import com.uade.ritmofitapi.dto.StudentDto;
import com.uade.ritmofitapi.service.UserMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("")
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserDto).toList();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable String id) {
        return userRepository.findById(id).map(userMapper::toUserDto).orElse(null);
    }

    @PostMapping("/student")
    public StudentDto createStudent(@RequestBody Student student) {
        log.info("hola");
        Student saved = userRepository.save(student);
        return userMapper.toStudentDto(saved);
    }

    @PostMapping("/professor")
    public ProfessorDto createProfessor(@RequestBody Professor professor) {
        Professor saved = userRepository.save(professor);
        return userMapper.toProfessorDto(saved);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable String id, @RequestBody User user) {
        user.setId(id);
        User saved = userRepository.save(user);
        return userMapper.toUserDto(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
    }
}
