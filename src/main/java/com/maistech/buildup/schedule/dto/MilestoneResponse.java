package com.maistech.buildup.schedule.dto;

import com.maistech.buildup.schedule.MilestoneStatus;
import com.maistech.buildup.schedule.MilestoneType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MilestoneResponse(
    UUID id,
    String name,
    String description,
    LocalDate plannedDate,
    LocalDate actualDate,
    MilestoneStatus status,
    MilestoneType type,
    Integer completionPercentage,
    UUID projectId,
    String projectName,
    Integer orderIndex,
    Boolean isOverdue,
    Integer daysUntilDue,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
