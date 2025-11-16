package com.maistech.buildup.task.dto;

import com.maistech.buildup.task.DependencyType;
import com.maistech.buildup.task.TaskStatus;
import java.util.UUID;

public record TaskDependencyDto(
    UUID dependencyId,
    UUID taskId,
    String taskName,
    TaskStatus taskStatus,
    DependencyType dependencyType
) {}
