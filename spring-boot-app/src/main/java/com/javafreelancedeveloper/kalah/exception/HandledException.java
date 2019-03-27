package com.javafreelancedeveloper.kalah.exception;

public class HandledException extends RuntimeException {

    public static final String MSG_UNEXPECTED = "We're sorry. Your game has experienced an unexpected exception.";

    public HandledException(String message) {
        super(message);
    }

    public HandledException(String message, Throwable cause) {
        super(message, cause);
    }
}
