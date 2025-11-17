package com.maistech.buildup.schedule.dto;

import com.maistech.buildup.schedule.MilestoneStatus;
import com.maistech.buildup.schedule.MilestoneType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public record UpdateMilestoneRequest(
    String name,
    String description,
    LocalDate plannedDate,
    LocalDate actualDate,
    MilestoneStatus status,
    MilestoneType type,

    @Min(0)
    @Max(100)
    Integer completionPercentage,

    Integer orderIndex
) {}
