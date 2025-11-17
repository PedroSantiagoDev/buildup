/**
 * Financial Management Module.
 * 
 * Handles expenses, payments, installments, and financial tracking for projects.
 * 
 * Public API:
 * - {@link com.maistech.buildup.financial.ExpenseEntity} - Expense entity
 * - {@link com.maistech.buildup.financial.ExpenseCategoryEntity} - Category entity
 * - {@link com.maistech.buildup.financial.domain.ExpenseRepository} - Expense repository
 * 
 * Internal:
 * - {@link com.maistech.buildup.financial.domain.ExpenseService} - Expense business logic
 * - {@link com.maistech.buildup.financial.domain.ExpenseInstallmentRepository} - Installment repository
 * - {@link com.maistech.buildup.financial.domain.PaymentMilestoneRepository} - Milestone repository
 */
@ApplicationModule(
    displayName = "Financial Management",
    allowedDependencies = {"auth", "project", "shared"}  // Expenses belong to projects
)
@org.springframework.lang.NonNullApi
package com.maistech.buildup.financial;

import org.springframework.modulith.ApplicationModule;
