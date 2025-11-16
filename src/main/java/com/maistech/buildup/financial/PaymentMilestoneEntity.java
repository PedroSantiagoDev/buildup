package com.maistech.buildup.financial;

import com.maistech.buildup.project.ProjectEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "payment_milestones")
@Getter
@Setter
public class PaymentMilestoneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @NotNull
    private ProjectEntity project;

    @Column(name = "milestone_number", nullable = false)
    private Integer milestoneNumber;

    private String description;

    @Positive
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal value;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    @NotNull
    private MilestoneStatus status = MilestoneStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ============ Domain Logic ============

    public boolean isLate() {
        return (
            dueDate != null &&
            LocalDate.now().isAfter(dueDate) &&
            status == MilestoneStatus.PENDING
        );
    }

    public void markAsPaid(LocalDate paymentDate) {
        this.status = MilestoneStatus.PAID;
        this.paymentDate = paymentDate;
    }

    public void updateStatusBasedOnDueDate() {
        if (status == MilestoneStatus.PENDING && isLate()) {
            this.status = MilestoneStatus.LATE;
        }
    }
}
