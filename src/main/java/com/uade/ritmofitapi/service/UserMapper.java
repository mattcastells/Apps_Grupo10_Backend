package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.UserDto;
import com.uade.ritmofitapi.dto.ProfessorDto;
import com.uade.ritmofitapi.dto.StudentDto;
import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.model.Professor;
import com.uade.ritmofitapi.model.Student;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setAge(user.getAge());
        dto.setGender(user.getGender());
        dto.setProfilePicture(user.getProfilePicture());
        if (user instanceof Professor) {
            dto.setRole("professor");
        } else if (user instanceof Student) {
            dto.setRole("client");
        } else {
            dto.setRole("user");
        }
        return dto;
    }

    public ProfessorDto toProfessorDto(Professor professor) {
        ProfessorDto dto = new ProfessorDto();
        dto.setId(professor.getId());
        dto.setEmail(professor.getEmail());
        dto.setName(professor.getName());
        dto.setAge(professor.getAge());
        dto.setGender(professor.getGender());
        dto.setProfilePicture(professor.getProfilePicture());
        dto.setRole("professor");
        dto.setClassTypes(professor.getClassTypes());
        // Map taughtClasses to IDs
        if (professor.getTaughtClasses() != null) {
            dto.setTaughtClassIds(professor.getTaughtClasses().stream().map(c -> c.getId()).toList());
        }
        return dto;
    }

    public StudentDto toStudentDto(Student student) {
        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setEmail(student.getEmail());
        dto.setName(student.getName());
        dto.setAge(student.getAge());
        dto.setGender(student.getGender());
        dto.setProfilePicture(student.getProfilePicture());
        dto.setRole("client");
        return dto;
    }
}
