package com.maistech.buildup.schedule;

import com.maistech.buildup.shared.entity.BaseEntity;
import com.maistech.buildup.task.TaskEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "phases")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PhaseEntity extends BaseEntity {

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    @NotNull
    private ScheduleEntity schedule;

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

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PhaseStatus status = PhaseStatus.PENDING;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @Column(name = "completion_percentage")
    @Builder.Default
    private Integer completionPercentage = 0;

    @Column(name = "duration_days")
    private Integer durationDays;

    @OneToMany(mappedBy = "phase", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TaskEntity> tasks = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    @PreUpdate
    private void calculateDuration() {
        if (startDate != null && endDate != null) {
            this.durationDays = (int) java.time.temporal.ChronoUnit.DAYS.between(
                startDate,
                endDate
            ) + 1;
        }
    }

    @PrePersist
    @PreUpdate
    private void syncCompanyId() {
        if (schedule != null && getCompanyId() == null) {
            setCompanyId(schedule.getCompanyId());
        }
    }

    public boolean isOverdue() {
        return endDate != null &&
            LocalDate.now().isAfter(endDate) &&
            status != PhaseStatus.COMPLETED &&
            status != PhaseStatus.CANCELLED;
    }

    public boolean isActive() {
        return status == PhaseStatus.IN_PROGRESS;
    }

    public void complete() {
        this.status = PhaseStatus.COMPLETED;
        this.completionPercentage = 100;
        this.actualEndDate = LocalDate.now();
    }

    public void start() {
        if (status == PhaseStatus.PENDING) {
            this.status = PhaseStatus.IN_PROGRESS;
            this.actualStartDate = LocalDate.now();
        }
    }

    public void updateProgress(Integer progress) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
        this.completionPercentage = progress;

        if (progress == 100 && status != PhaseStatus.COMPLETED) {
            complete();
        } else if (progress > 0 && status == PhaseStatus.PENDING) {
            start();
        }
    }
}
