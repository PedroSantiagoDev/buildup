package com.maistech.buildup.financial;

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

@Entity
@Table(name = "expense_installments")
@Getter
@Setter
public class ExpenseInstallmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    @NotNull
    private ExpenseEntity expense;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Positive
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

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ============ Domain Logic ============

    public boolean isOverdue() {
        return (
            dueDate != null &&
            LocalDate.now().isAfter(dueDate) &&
            status == ExpenseStatus.PENDING
        );
    }

    public void markAsPaid(LocalDate paymentDate, PaymentMethod method) {
        this.status = ExpenseStatus.PAID;
        this.paidDate = paymentDate;
        this.paymentMethod = method;
    }
}
