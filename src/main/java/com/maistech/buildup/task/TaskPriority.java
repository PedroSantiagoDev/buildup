package com.maistech.buildup.task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskPriority {
    LOW("Baixa"),
    MEDIUM("Média"),
    HIGH("Alta"),
    URGENT("Urgente");

    private final String displayName;

    TaskPriority(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static TaskPriority fromString(String value) {
        for (TaskPriority priority : TaskPriority.values()) {
            if (priority.displayName.equals(value) || priority.name().equals(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown TaskPriority: " + value);
    }
}
