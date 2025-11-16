package com.maistech.buildup.financial.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinancialSummaryResponse(
    BigDecimal totalExpenses,
    BigDecimal totalPaid,
    BigDecimal totalPending,
    List<CategorySummary> expensesByCategory,
    Long overdueExpensesCount
) {}
