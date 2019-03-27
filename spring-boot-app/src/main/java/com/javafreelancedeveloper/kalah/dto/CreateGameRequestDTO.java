package com.javafreelancedeveloper.kalah.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class CreateGameRequestDTO {

    @NotEmpty
    @Size(min = 5, max = 25)
    private final String name;
}
