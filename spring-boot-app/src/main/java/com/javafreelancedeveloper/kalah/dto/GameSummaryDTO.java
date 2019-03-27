package com.javafreelancedeveloper.kalah.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class GameSummaryDTO {

    private final UUID id;
    private final String name;
    private final String status;
    private final Long createdTimestamp;
    private final Long updatedTimestamp;
}
