package com.maistech.buildup.task.dto;

import com.maistech.buildup.task.TaskPriority;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTaskRequest(
    @NotEmpty(message = "Task name is required") String name,

    String description,
    LocalDate startDate,
    LocalDate endDate,
    Integer durationDays,
    TaskPriority priority,
    UUID assignedTo,

    @Min(0) @Max(100) Integer progressPercentage
) {}
