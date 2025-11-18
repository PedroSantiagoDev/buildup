package com.maistech.buildup.schedule.dto;

import java.time.LocalDate;
import java.util.List;

public record CriticalPathResponse(
    List<TaskInCriticalPath> tasks,
    Integer totalDuration,
    LocalDate estimatedStartDate,
    LocalDate estimatedEndDate
) {
    public record TaskInCriticalPath(
        String taskId,
        String taskTitle,
        Integer duration,
        LocalDate earliestStart,
        LocalDate earliestFinish,
        LocalDate latestStart,
        LocalDate latestFinish,
        Integer slack
    ) {}
}
