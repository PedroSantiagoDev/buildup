package com.maistech.buildup.financial.dto;

import com.maistech.buildup.financial.PaymentMethod;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateExpenseRequest(
    String description,

    @Positive(message = "Amount must be positive") BigDecimal amount,

    LocalDate dueDate,
    PaymentMethod paymentMethod,
    String supplier,
    String invoiceNumber,
    String invoiceUrl,
    String notes
) {}
