package com.javafreelancedeveloper.kalah.exception;

public class GameNotPendingException extends HandledException {

    private static final String MSG = "Could not join game. Game is no longer waiting for a player to join. Please try another game.";

    public GameNotPendingException() {
        super(MSG);
    }
}
