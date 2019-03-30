package com.javafreelancedeveloper.kalah.dto;

import lombok.Data;

@Data
public class GameUpdateDTO {


    private final GameUpdateType updateType;
    private final GameSummaryDTO game;
}
