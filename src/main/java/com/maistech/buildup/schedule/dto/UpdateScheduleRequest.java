package com.maistech.buildup.schedule.dto;

import com.maistech.buildup.schedule.ScheduleStatus;
import java.time.LocalDate;

public record UpdateScheduleRequest(
    LocalDate startDate,
    LocalDate endDate,
    LocalDate actualStartDate,
    LocalDate actualEndDate,
    ScheduleStatus status,
    String notes
) {}
