package com.maistech.buildup.task.dto;

import com.maistech.buildup.task.TaskPriority;
import com.maistech.buildup.task.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TaskResponse(
    UUID id,
    UUID projectId,
    String projectName,
    String name,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    Integer durationDays,
    TaskStatus status,
    TaskPriority priority,
    Integer progressPercentage,
    AssignedUserDto assignedTo,
    boolean isOverdue,
    Long daysUntilDueDate,
    boolean hasBlockingDependencies,
    List<TaskDependencyDto> dependencies,
    UUID createdById,
    String createdByName,
    LocalDateTime createdAt
) {}
