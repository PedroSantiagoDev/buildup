package com.maistech.buildup.schedule.dto;

import com.maistech.buildup.schedule.MilestoneType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateMilestoneRequest(
    @NotBlank(message = "Name is required") String name,

    String description,

    @NotNull(message = "Planned date is required") LocalDate plannedDate,

    MilestoneType type,

    @Min(0) @Max(100) Integer completionPercentage,

    Integer orderIndex
) {}
