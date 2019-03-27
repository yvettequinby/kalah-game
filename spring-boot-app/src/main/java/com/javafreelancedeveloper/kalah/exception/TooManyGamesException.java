package com.javafreelancedeveloper.kalah.exception;

public class TooManyGamesException extends HandledException {

    private static final String MSG = "There are too many games currently active. Please wait and try again later.";

    public TooManyGamesException() {
        super(MSG);
    }
}
