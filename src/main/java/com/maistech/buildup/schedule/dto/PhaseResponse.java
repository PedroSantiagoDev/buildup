package com.maistech.buildup.schedule.dto;

import com.maistech.buildup.schedule.PhaseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PhaseResponse(
    UUID id,
    String name,
    String description,
    UUID scheduleId,
    LocalDate startDate,
    LocalDate endDate,
    LocalDate actualStartDate,
    LocalDate actualEndDate,
    PhaseStatus status,
    Integer orderIndex,
    Integer completionPercentage,
    Integer durationDays,
    Integer totalTasks,
    Integer completedTasks,
    Boolean isOverdue,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
