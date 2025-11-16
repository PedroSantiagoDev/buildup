package com.maistech.buildup.financial;

import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.project.ProjectEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Table(name = "expenses")
@Getter
@Setter
public class ExpenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @NotNull
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull
    private ExpenseCategoryEntity category;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, length = 500)
    private String description;

    @Positive(message = "Amount must be positive")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    @NotNull
    private ExpenseStatus status = ExpenseStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    private PaymentMethod paymentMethod;

    private String supplier;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "invoice_url", length = 500)
    private String invoiceUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "has_installments")
    private Boolean hasInstallments = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @OneToMany(
        mappedBy = "expense",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ExpenseInstallmentEntity> installments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ============ Domain Logic ============

    public boolean isOverdue() {
        return (
            dueDate != null &&
            LocalDate.now().isAfter(dueDate) &&
            status == ExpenseStatus.PENDING
        );
    }

    public boolean isPaid() {
        return status == ExpenseStatus.PAID && paidDate != null;
    }

    public void markAsPaid(LocalDate paymentDate, PaymentMethod method) {
        if (status == ExpenseStatus.CANCELLED) {
            throw new IllegalStateException(
                "Cannot mark cancelled expense as paid"
            );
        }
        this.status = ExpenseStatus.PAID;
        this.paidDate = paymentDate;
        this.paymentMethod = method;
    }

    public void cancel() {
        if (status == ExpenseStatus.PAID) {
            throw new IllegalStateException("Cannot cancel paid expense");
        }
        this.status = ExpenseStatus.CANCELLED;
    }

    public void updateStatusBasedOnDueDate() {
        if (status == ExpenseStatus.PENDING && isOverdue()) {
            this.status = ExpenseStatus.OVERDUE;
        }
    }

    public BigDecimal getTotalPaid() {
        if (hasInstallments && !installments.isEmpty()) {
            return installments
                .stream()
                .filter(i -> i.getStatus() == ExpenseStatus.PAID)
                .map(ExpenseInstallmentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return isPaid() ? amount : BigDecimal.ZERO;
    }

    public BigDecimal getRemainingAmount() {
        if (hasInstallments && !installments.isEmpty()) {
            return amount.subtract(getTotalPaid());
        }
        return isPaid() ? BigDecimal.ZERO : amount;
    }

    public int getTotalInstallments() {
        return hasInstallments ? installments.size() : 1;
    }

    public int getPaidInstallments() {
        if (!hasInstallments) {
            return isPaid() ? 1 : 0;
        }
        return (int) installments
            .stream()
            .filter(i -> i.getStatus() == ExpenseStatus.PAID)
            .count();
    }

    public boolean isFullyPaid() {
        if (!hasInstallments) {
            return isPaid();
        }
        return installments
            .stream()
            .allMatch(i -> i.getStatus() == ExpenseStatus.PAID);
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
}
