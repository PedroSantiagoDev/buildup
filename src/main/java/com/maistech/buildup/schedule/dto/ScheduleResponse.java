package com.maistech.buildup.schedule.dto;

import com.maistech.buildup.schedule.ScheduleStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ScheduleResponse(
    UUID id,
    UUID projectId,
    String projectName,
    LocalDate startDate,
    LocalDate endDate,
    LocalDate actualStartDate,
    LocalDate actualEndDate,
    Integer totalDurationDays,
    Integer completedPercentage,
    Integer totalTasks,
    Integer completedTasks,
    Integer overdueTasks,
    Integer criticalPathDuration,
    ScheduleStatus status,
    Boolean isOnTrack,
    String notes,
    LocalDateTime lastCalculatedAt,
    List<MilestoneResponse> milestones,
    Integer daysRemaining,
    Integer daysElapsed,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
