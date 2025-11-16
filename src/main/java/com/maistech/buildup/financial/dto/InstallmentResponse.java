package com.maistech.buildup.financial.dto;

import com.maistech.buildup.financial.ExpenseStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InstallmentResponse(
    UUID id,
    Integer installmentNumber,
    BigDecimal amount,
    LocalDate dueDate,
    LocalDate paidDate,
    ExpenseStatus status
) {}
