package com.maistech.buildup.schedule;

import com.maistech.buildup.project.ProjectEntity;
import com.maistech.buildup.shared.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "milestones")
@Getter
@Setter
public class MilestoneEntity extends BaseEntity {

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Column(name = "actual_date")
    private LocalDate actualDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilestoneStatus status = MilestoneStatus.PENDING;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilestoneType type = MilestoneType.GENERAL;

    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(name = "order_index")
    private Integer orderIndex;
}
