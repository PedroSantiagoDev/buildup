package com.maistech.buildup.project;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Project status enumeration")
public enum ProjectStatus {
    @Schema(description = "Project is currently in progress")
    IN_PROGRESS("Em Andamento"),
    
    @Schema(description = "Project has been completed")
    COMPLETED("Concluído"),
    
    @Schema(description = "Project is temporarily on hold/paused")
    ON_HOLD("Pausado"),
    
    @Schema(description = "Project has been cancelled")
    CANCELLED("Cancelado");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
