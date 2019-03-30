package com.javafreelancedeveloper.kalah.domain;

public enum GameStatus {

    PENDING("WAITING FOR SECOND PLAYER TO JOIN"),
    PLAYER_ONE_TURN("PLAYER ONE'S TURN"),
    PLAYER_TWO_TURN("PLAYER TWO'S TURN"),
    GAME_OVER("GAME OVER"),
    TIME_OUT_INACTIVE("GAME CANCELLED DUE TO INACTIVITY"),
    TIME_OUT_DURATION("GAME CANCELLED DUE TO EXCESS DURATION");

    private final String description;

    GameStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
