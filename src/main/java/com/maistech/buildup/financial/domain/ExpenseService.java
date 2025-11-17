package com.maistech.buildup.financial.domain;

import com.maistech.buildup.financial.*;
import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.auth.domain.UserRepository;
import com.maistech.buildup.financial.dto.*;
import com.maistech.buildup.project.ProjectEntity;
import com.maistech.buildup.project.ProjectNotFoundException;
import com.maistech.buildup.project.domain.ProjectRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseInstallmentRepository installmentRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ExpenseService(
        ExpenseRepository expenseRepository,
        ExpenseInstallmentRepository installmentRepository,
        ExpenseCategoryRepository categoryRepository,
        ProjectRepository projectRepository,
        UserRepository userRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.installmentRepository = installmentRepository;
        this.categoryRepository = categoryRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public ExpenseResponse createExpense(
        UUID companyId,
        UUID projectId,
        UUID userId,
        CreateExpenseRequest request
    ) {
        ProjectEntity project = findProjectInCompanyOrThrow(
            projectId,
            companyId
        );
        UserEntity user = userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ExpenseCategoryEntity category = categoryRepository
            .findById(request.categoryId())
            .orElseThrow(() ->
                new IllegalArgumentException("Category not found")
            );

        ExpenseEntity expense = new ExpenseEntity();
        expense.setProject(project);
        expense.setCategory(category);
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setDueDate(request.dueDate());
        expense.setStatus(ExpenseStatus.PENDING);
        expense.setPaymentMethod(request.paymentMethod());
        expense.setSupplier(request.supplier());
        expense.setInvoiceNumber(request.invoiceNumber());
        expense.setInvoiceUrl(request.invoiceUrl());
        expense.setNotes(request.notes());
        expense.setCreatedBy(user);

        if (
            request.installments() != null && !request.installments().isEmpty()
        ) {
            expense.setHasInstallments(true);
            expense = expenseRepository.save(expense);
            createInstallments(expense, request.installments());
        } else {
            expense = expenseRepository.save(expense);
        }

        return mapToResponse(expense);
    }

    public ExpenseResponse updateExpense(
        UUID companyId,
        UUID projectId,
        UUID expenseId,
        UpdateExpenseRequest request
    ) {
        ExpenseEntity expense = findExpenseInProjectOrThrow(
            expenseId,
            projectId,
            companyId
        );

        if (request.description() != null) {
            expense.setDescription(request.description());
        }
        if (request.amount() != null) {
            expense.setAmount(request.amount());
        }
        if (request.dueDate() != null) {
            expense.setDueDate(request.dueDate());
        }
        if (request.paymentMethod() != null) {
            expense.setPaymentMethod(request.paymentMethod());
        }
        if (request.supplier() != null) {
            expense.setSupplier(request.supplier());
        }
        if (request.invoiceNumber() != null) {
            expense.setInvoiceNumber(request.invoiceNumber());
        }
        if (request.invoiceUrl() != null) {
            expense.setInvoiceUrl(request.invoiceUrl());
        }
        if (request.notes() != null) {
            expense.setNotes(request.notes());
        }

        expense = expenseRepository.save(expense);
        return mapToResponse(expense);
    }

    public void deleteExpense(UUID companyId, UUID projectId, UUID expenseId) {
        ExpenseEntity expense = findExpenseInProjectOrThrow(
            expenseId,
            projectId,
            companyId
        );
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(
        UUID companyId,
        UUID projectId,
        UUID expenseId
    ) {
        ExpenseEntity expense = findExpenseInProjectOrThrow(
            expenseId,
            projectId,
            companyId
        );
        return mapToResponse(expense);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> listProjectExpenses(
        UUID companyId,
        UUID projectId,
        Pageable pageable
    ) {
        findProjectInCompanyOrThrow(projectId, companyId);
        return expenseRepository
            .findByProjectIdOrderByDueDateDesc(projectId, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listOverdueExpenses(
        UUID companyId,
        UUID projectId
    ) {
        findProjectInCompanyOrThrow(projectId, companyId);
        return expenseRepository
            .findOverdueExpenses(projectId, LocalDate.now())
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listExpensesByCategory(
        UUID companyId,
        UUID projectId,
        UUID categoryId
    ) {
        findProjectInCompanyOrThrow(projectId, companyId);
        return expenseRepository
            .findByProjectIdAndCategoryId(projectId, categoryId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public ExpenseResponse markAsPaid(
        UUID companyId,
        UUID projectId,
        UUID expenseId,
        MarkAsPaidRequest request
    ) {
        ExpenseEntity expense = findExpenseInProjectOrThrow(
            expenseId,
            projectId,
            companyId
        );
        expense.markAsPaid(request.paymentDate(), request.paymentMethod());
        expense = expenseRepository.save(expense);
        return mapToResponse(expense);
    }

    public ExpenseResponse cancelExpense(
        UUID companyId,
        UUID projectId,
        UUID expenseId
    ) {
        ExpenseEntity expense = findExpenseInProjectOrThrow(
            expenseId,
            projectId,
            companyId
        );
        expense.cancel();
        expense = expenseRepository.save(expense);
        return mapToResponse(expense);
    }

    @Transactional(readOnly = true)
    public FinancialSummaryResponse getFinancialSummary(
        UUID companyId,
        UUID projectId
    ) {
        findProjectInCompanyOrThrow(projectId, companyId);

        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByProject(
            projectId
        );
        if (totalExpenses == null) {
            totalExpenses = BigDecimal.ZERO;
        }

        BigDecimal totalPaid = expenseRepository.getTotalPaidByProject(
            projectId
        );
        if (totalPaid == null) {
            totalPaid = BigDecimal.ZERO;
        }

        BigDecimal totalPending = totalExpenses.subtract(totalPaid);

        List<Object[]> categoryData = expenseRepository.getExpensesByCategory(
            projectId
        );
        List<CategorySummary> byCategory = categoryData
            .stream()
            .map(row ->
                new CategorySummary((String) row[0], (BigDecimal) row[1])
            )
            .collect(Collectors.toList());

        long overdueCount = expenseRepository
            .findOverdueExpenses(projectId, LocalDate.now())
            .size();

        return new FinancialSummaryResponse(
            totalExpenses,
            totalPaid,
            totalPending,
            byCategory,
            overdueCount
        );
    }

    private void createInstallments(
        ExpenseEntity expense,
        List<InstallmentRequest> installments
    ) {
        for (int i = 0; i < installments.size(); i++) {
            InstallmentRequest req = installments.get(i);
            ExpenseInstallmentEntity installment =
                new ExpenseInstallmentEntity();
            installment.setExpense(expense);
            installment.setInstallmentNumber(i + 1);
            installment.setAmount(req.amount());
            installment.setDueDate(req.dueDate());
            installment.setStatus(ExpenseStatus.PENDING);
            installmentRepository.save(installment);
        }
    }

    private ExpenseEntity findExpenseInProjectOrThrow(
        UUID expenseId,
        UUID projectId,
        UUID companyId
    ) {
        ExpenseEntity expense = expenseRepository
            .findById(expenseId)
            .orElseThrow(() ->
                new ExpenseNotFoundException("Expense not found: " + expenseId)
            );

        if (!expense.getProject().getId().equals(projectId)) {
            throw new ExpenseNotFoundException(
                "Expense does not belong to this project"
            );
        }

        if (!expense.getProject().getCompanyId().equals(companyId)) {
            throw new IllegalStateException(
                "Expense does not belong to your company"
            );
        }

        return expense;
    }

    private ProjectEntity findProjectInCompanyOrThrow(
        UUID projectId,
        UUID companyId
    ) {
        return projectRepository
            .findByIdAndCompanyId(projectId, companyId)
            .orElseThrow(() ->
                new ProjectNotFoundException(
                    "Project not found or does not belong to this company"
                )
            );
    }

    private ExpenseResponse mapToResponse(ExpenseEntity expense) {
        List<ExpenseInstallmentEntity> installmentEntities =
            installmentRepository.findByExpenseIdOrderByInstallmentNumberAsc(
                expense.getId()
            );

        List<InstallmentResponse> installments = installmentEntities
            .stream()
            .map(i ->
                new InstallmentResponse(
                    i.getId(),
                    i.getInstallmentNumber(),
                    i.getAmount(),
                    i.getDueDate(),
                    i.getPaidDate(),
                    i.getStatus()
                )
            )
            .collect(Collectors.toList());

        return new ExpenseResponse(
            expense.getId(),
            expense.getProject().getId(),
            expense.getProject().getName(),
            expense.getCategory().getId(),
            expense.getCategory().getName(),
            expense.getDescription(),
            expense.getAmount(),
            expense.getDueDate(),
            expense.getPaidDate(),
            expense.getStatus(),
            expense.getPaymentMethod(),
            expense.getSupplier(),
            expense.getInvoiceNumber(),
            expense.getInvoiceUrl(),
            expense.getNotes(),
            expense.getHasInstallments(),
            installments,
            expense.getTotalPaid(),
            expense.getRemainingAmount(),
            expense.isOverdue(),
            expense.getDaysUntilDueDate(),
            expense.getCreatedBy().getId(),
            expense.getCreatedBy().getName(),
            expense.getCreatedAt()
        );
    }
}
