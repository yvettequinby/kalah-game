package com.javafreelancedeveloper.kalah.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class GameMoveDTO {

    private final UUID gameId;
    private final UUID playerId;
    private final Integer pitNumber;
}
