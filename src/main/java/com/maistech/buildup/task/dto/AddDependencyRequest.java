package com.maistech.buildup.task.dto;

import com.maistech.buildup.task.DependencyType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddDependencyRequest(
    @NotNull(message = "Depends on task ID is required") UUID dependsOnTaskId,

    DependencyType dependencyType
) {
    public AddDependencyRequest {
        if (dependencyType == null) {
            dependencyType = DependencyType.FINISH_TO_START;
        }
    }
}
