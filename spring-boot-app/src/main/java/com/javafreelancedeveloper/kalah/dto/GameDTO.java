package com.javafreelancedeveloper.kalah.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class GameDTO {

    private final UUID id;
    private final String name;
    private final String status;
    private final String statusDescription;
    private final Long createdTimestamp;
    private final Long updatedTimestamp;
    private final UUID playerId;
    private final String playerDesignation;
    private final Map<Integer, Integer> state;

}
