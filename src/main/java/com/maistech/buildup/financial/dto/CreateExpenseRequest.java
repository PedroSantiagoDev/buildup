package com.maistech.buildup.financial.dto;

import com.maistech.buildup.financial.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateExpenseRequest(
    @NotNull(message = "Category is required") UUID categoryId,

    @NotBlank(message = "Description is required") String description,

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    BigDecimal amount,

    @NotNull(message = "Due date is required") LocalDate dueDate,

    PaymentMethod paymentMethod,
    String supplier,
    String invoiceNumber,
    String invoiceUrl,
    String notes,
    List<InstallmentRequest> installments
) {}
