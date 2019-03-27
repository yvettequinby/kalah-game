package com.javafreelancedeveloper.kalah.exception;

public class GameNotFoundException extends HandledException {

    private static final String MSG = "Game could not be found. It may no longer exist. Please try another game.";

    public GameNotFoundException() {
        super(MSG);
    }
}
