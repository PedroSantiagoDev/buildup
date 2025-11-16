package com.maistech.buildup.task;

import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.project.ProjectEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @NotNull
    private ProjectEntity project;

    @NotBlank(message = "Task name is required")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    @NotNull
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private UserEntity assignedTo;

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @OneToMany(
        mappedBy = "task",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<TaskDependencyEntity> dependencies = new ArrayList<>();

    @OneToMany(
        mappedBy = "dependsOnTask",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<TaskDependencyEntity> dependentTasks = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ============ Domain Logic ============

    public boolean isOverdue() {
        return (
            endDate != null &&
            LocalDate.now().isAfter(endDate) &&
            status != TaskStatus.COMPLETED &&
            status != TaskStatus.CANCELLED
        );
    }

    public boolean canBeCompleted() {
        return status == TaskStatus.IN_PROGRESS || status == TaskStatus.PENDING;
    }

    public void complete() {
        if (!canBeCompleted()) {
            throw new IllegalStateException(
                "Task cannot be completed in status: " + status
            );
        }
        this.status = TaskStatus.COMPLETED;
        this.progressPercentage = 100;
    }

    public void start() {
        if (status != TaskStatus.PENDING) {
            throw new IllegalStateException(
                "Only pending tasks can be started"
            );
        }
        this.status = TaskStatus.IN_PROGRESS;
        if (this.progressPercentage == 0) {
            this.progressPercentage = 5;
        }
    }

    public void updateProgress(Integer newProgress) {
        if (newProgress < 0 || newProgress > 100) {
            throw new IllegalArgumentException(
                "Progress must be between 0 and 100"
            );
        }
        this.progressPercentage = newProgress;

        if (newProgress == 100 && canBeCompleted()) {
            this.status = TaskStatus.COMPLETED;
        } else if (newProgress > 0 && status == TaskStatus.PENDING) {
            this.status = TaskStatus.IN_PROGRESS;
        }
    }

    public long getDaysUntilDueDate() {
        if (endDate == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(
            LocalDate.now(),
            endDate
        );
    }

    public boolean isAssignedTo(UUID userId) {
        return assignedTo != null && assignedTo.getId().equals(userId);
    }

    public boolean isHighPriority() {
        return priority == TaskPriority.HIGH || priority == TaskPriority.URGENT;
    }

    public boolean hasBlockingDependencies() {
        return dependencies
            .stream()
            .anyMatch(
                dep ->
                    dep.getDependsOnTask().getStatus() != TaskStatus.COMPLETED
            );
    }

    public void calculateDurationFromDates() {
        if (startDate != null && endDate != null) {
            this.durationDays =
                (int) java.time.temporal.ChronoUnit.DAYS.between(
                    startDate,
                    endDate
                ) +
                1;
        }
    }

    public void calculateEndDateFromDuration() {
        if (startDate != null && durationDays != null) {
            this.endDate = startDate.plusDays(durationDays - 1);
        }
    }
}
