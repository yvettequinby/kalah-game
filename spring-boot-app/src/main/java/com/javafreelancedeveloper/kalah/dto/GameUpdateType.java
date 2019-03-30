package com.javafreelancedeveloper.kalah.dto;

public enum GameUpdateType {

    APG("ADD PENDING GAME"), RPG("REMOVE PENDING GAME"), AAG("ADD ACTIVE GAME"), RAG("REMOVE ACTIVE GAME");

    private String displayName;

    GameUpdateType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
