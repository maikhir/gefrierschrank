package com.gefrierschrank.app.entity;

public enum ExpiryType {
    USE_BY("Verbrauchen bis"),
    BEST_BEFORE("Mindestens haltbar bis");
    
    private final String displayName;
    
    ExpiryType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}