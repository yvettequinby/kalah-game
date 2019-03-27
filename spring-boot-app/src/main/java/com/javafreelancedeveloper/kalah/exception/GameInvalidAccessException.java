package com.javafreelancedeveloper.kalah.exception;

public class GameInvalidAccessException extends HandledException {

    private static final String MSG = "You are not a player in this game. Invalid access!";

    public GameInvalidAccessException() {
        super(MSG);
    }
}
