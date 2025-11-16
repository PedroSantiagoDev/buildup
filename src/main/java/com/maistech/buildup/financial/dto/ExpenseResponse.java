package com.maistech.buildup.financial.dto;

import com.maistech.buildup.financial.ExpenseStatus;
import com.maistech.buildup.financial.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ExpenseResponse(
    UUID id,
    UUID projectId,
    String projectName,
    UUID categoryId,
    String categoryName,
    String description,
    BigDecimal amount,
    LocalDate dueDate,
    LocalDate paidDate,
    ExpenseStatus status,
    PaymentMethod paymentMethod,
    String supplier,
    String invoiceNumber,
    String invoiceUrl,
    String notes,
    Boolean hasInstallments,
    List<InstallmentResponse> installments,
    BigDecimal totalPaid,
    BigDecimal remainingAmount,
    boolean isOverdue,
    Long daysUntilDueDate,
    UUID createdById,
    String createdByName,
    LocalDateTime createdAt
) {}
