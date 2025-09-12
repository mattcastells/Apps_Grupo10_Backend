package com.uade.ritmofitapi.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProfessorDto extends UserDto {
    private List<String> classTypes;
    private List<String> taughtClassIds; // IDs de clases dadas
}
