package com.maistech.buildup.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Project status enumeration")
public enum ProjectStatus {
    @Schema(description = "Project is currently in progress")
    IN_PROGRESS("In Progress"),

    @Schema(description = "Project has been completed")
    COMPLETED("Completed"),

    @Schema(description = "Project is temporarily on hold/paused")
    ON_HOLD("On Hold"),

    @Schema(description = "Project has been cancelled")
    CANCELLED("Cancelled");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ProjectStatus fromString(String value) {
        for (ProjectStatus status : ProjectStatus.values()) {
            if (status.displayName.equals(value) || status.name().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ProjectStatus: " + value);
    }
}
