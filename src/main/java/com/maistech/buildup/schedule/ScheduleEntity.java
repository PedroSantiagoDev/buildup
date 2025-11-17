package com.maistech.buildup.schedule;

import com.maistech.buildup.project.ProjectEntity;
import com.maistech.buildup.shared.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@Getter
@Setter
public class ScheduleEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private ProjectEntity project;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "total_duration_days")
    private Integer totalDurationDays;

    @Column(name = "completed_percentage")
    private Integer completedPercentage = 0;

    @Column(name = "total_tasks")
    private Integer totalTasks = 0;

    @Column(name = "completed_tasks")
    private Integer completedTasks = 0;

    @Column(name = "overdue_tasks")
    private Integer overdueTasks = 0;

    @Column(name = "critical_path_duration")
    private Integer criticalPathDuration;

    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status = ScheduleStatus.DRAFT;

    @Column(name = "is_on_track")
    private Boolean isOnTrack = true;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
