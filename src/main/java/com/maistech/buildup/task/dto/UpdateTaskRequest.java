package com.maistech.buildup.task.dto;

import com.maistech.buildup.task.TaskPriority;
import com.maistech.buildup.task.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateTaskRequest(
    String name,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    Integer durationDays,
    TaskStatus status,
    TaskPriority priority,
    UUID assignedTo,

    @Min(0) @Max(100) Integer progressPercentage
) {}
