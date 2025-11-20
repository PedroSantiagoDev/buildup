package com.maistech.buildup.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreatePhaseRequest(
    @NotBlank(message = "Phase name is required")
    String name,

    String description,

    @NotNull(message = "Start date is required")
    LocalDate startDate,

    @NotNull(message = "End date is required")
    LocalDate endDate,

    Integer orderIndex,

    String notes
) {}
