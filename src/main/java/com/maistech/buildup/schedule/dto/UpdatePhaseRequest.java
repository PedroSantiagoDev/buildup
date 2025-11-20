package com.maistech.buildup.schedule.dto;

import com.maistech.buildup.schedule.PhaseStatus;

import java.time.LocalDate;

public record UpdatePhaseRequest(
    String name,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    LocalDate actualStartDate,
    LocalDate actualEndDate,
    PhaseStatus status,
    Integer orderIndex,
    Integer completionPercentage,
    String notes
) {}
