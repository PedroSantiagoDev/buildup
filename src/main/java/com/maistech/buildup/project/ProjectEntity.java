package com.maistech.buildup.project;

import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.company.CompanyEntity;
import com.maistech.buildup.shared.tenant.TenantAware;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
@Table(name = "projects")
@Getter
@Setter
public class ProjectEntity implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Project name is required")
    @Column(nullable = false)
    private String name;

    @Column(name = "client_name")
    private String clientName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "contract_value", precision = 15, scale = 2)
    private BigDecimal contractValue;

    @Column(name = "down_payment", precision = 15, scale = 2)
    private BigDecimal downPayment;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    @NotNull
    private ProjectStatus status = ProjectStatus.IN_PROGRESS;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private CompanyEntity company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @OneToMany(
        mappedBy = "project",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ProjectMemberEntity> members = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public UUID getCompanyId() {
        return companyId;
    }

    @Override
    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public boolean isOverdue() {
        return (
            dueDate != null &&
            LocalDate.now().isAfter(dueDate) &&
            status == ProjectStatus.IN_PROGRESS
        );
    }

    public boolean canBeCompleted() {
        return status == ProjectStatus.IN_PROGRESS;
    }

    public void complete() {
        if (!canBeCompleted()) {
            throw new IllegalStateException(
                "Project cannot be completed in status: " + status
            );
        }
        this.status = ProjectStatus.COMPLETED;
    }

    public BigDecimal getRemainingPayment() {
        if (contractValue == null) {
            return BigDecimal.ZERO;
        }
        if (downPayment == null) {
            return contractValue;
        }
        return contractValue.subtract(downPayment);
    }

    public long getDaysUntilDueDate() {
        if (dueDate == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(
            LocalDate.now(),
            dueDate
        );
    }

    public void addMember(UserEntity user, String role, boolean canEdit) {
        ProjectMemberEntity member = new ProjectMemberEntity();
        member.setProject(this);
        member.setUser(user);
        member.setRole(role);
        member.setCanEdit(canEdit);
        members.add(member);
    }

    public boolean isMember(UUID userId) {
        return members
            .stream()
            .anyMatch(m -> m.getUser().getId().equals(userId));
    }

    public boolean canUserEdit(UUID userId) {
        return members
            .stream()
            .filter(m -> m.getUser().getId().equals(userId))
            .findFirst()
            .map(ProjectMemberEntity::getCanEdit)
            .orElse(false);
    }
}
