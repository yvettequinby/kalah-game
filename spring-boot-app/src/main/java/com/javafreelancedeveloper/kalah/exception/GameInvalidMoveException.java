package com.javafreelancedeveloper.kalah.exception;

public class GameInvalidMoveException extends HandledException {

    public static final String MSG_NOT_YOUR_TURN = "Move not allowed. It is not your turn.";
    public static final String MSG_NOT_CORRECT_STATE = "Move not allowed. This game isn't active anymore.";
    public static final String MSG_INVALID_PIT = "Move not allowed. Invalid pit selected.";
    public static final String MSG_NO_STONES = "Move not allowed. Selected pit has no stones.";

    public GameInvalidMoveException(String message) {
        super(message);
    }
}
