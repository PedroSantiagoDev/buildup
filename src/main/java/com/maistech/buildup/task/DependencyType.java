package com.maistech.buildup.task;

public enum DependencyType {
    FINISH_TO_START("Término para Início"),
    START_TO_START("Início para Início"),
    FINISH_TO_FINISH("Término para Término"),
    START_TO_FINISH("Início para Término");

    private final String displayName;

    DependencyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
