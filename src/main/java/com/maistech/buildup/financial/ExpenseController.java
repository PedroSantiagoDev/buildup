package com.maistech.buildup.financial;

import com.maistech.buildup.financial.dto.*;
import com.maistech.buildup.financial.domain.ExpenseService;
import com.maistech.buildup.shared.security.JWTUserData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/companies/{companyId}/projects/{projectId}/expenses")
@SecurityRequirement(name = "bearer-jwt")
@Tag(
    name = "Expenses",
    description = "Financial expense management for construction projects. Track costs, payments, and generate financial reports."
)
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Create new expense",
        description = "Creates a new expense entry for the project. Supports single payments or installments."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Expense created successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExpenseResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires ADMIN or MANAGER role"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Project or category not found"
            ),
        }
    )
    public ResponseEntity<ExpenseResponse> createExpense(
        @Parameter(description = "Company ID") @PathVariable UUID companyId,
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Valid @RequestBody CreateExpenseRequest request,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();

        ExpenseResponse expense = expenseService.createExpense(
            companyId,
            projectId,
            userData.userId(),
            request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(expense);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "List project expenses",
        description = "Returns paginated list of all expenses for the project, ordered by due date (most recent first)."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Expenses retrieved successfully"
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(
                responseCode = "404",
                description = "Project not found"
            ),
        }
    )
    public ResponseEntity<Page<ExpenseResponse>> listExpenses(
        @Parameter(description = "Company ID") @PathVariable UUID companyId,
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @PageableDefault(
            size = 20,
            sort = "dueDate",
            direction = Sort.Direction.DESC
        ) Pageable pageable,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        Page<ExpenseResponse> expenses = expenseService.listProjectExpenses(
            companyId,
            projectId,
            pageable
        );
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{expenseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Get expense by ID",
        description = "Returns detailed information about a specific expense including installments if applicable."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Expense found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExpenseResponse.class)
                )
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(
                responseCode = "404",
                description = "Expense not found"
            ),
        }
    )
    public ResponseEntity<ExpenseResponse> getExpense(
        @Parameter(description = "Company ID") @PathVariable UUID companyId,
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        ExpenseResponse expense = expenseService.getExpenseById(
            companyId,
            projectId,
            expenseId
        );
        return ResponseEntity.ok(expense);
    }

    @PutMapping("/{expenseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Update expense",
        description = "Updates expense information. All fields are optional - only provided fields will be updated."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Expense updated successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires ADMIN or MANAGER role"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Expense not found"
            ),
        }
    )
    public ResponseEntity<ExpenseResponse> updateExpense(
        @Parameter(description = "Company ID") @PathVariable UUID companyId,
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
        @Valid @RequestBody UpdateExpenseRequest request,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        ExpenseResponse expense = expenseService.updateExpense(
            companyId,
            projectId,
            expenseId,
            request
        );
        return ResponseEntity.ok(expense);
    }

    @DeleteMapping("/{expenseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Delete expense",
        description = "Permanently deletes an expense and all associated installments. This action cannot be undone."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "204",
                description = "Expense deleted successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires ADMIN or MANAGER role"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Expense not found"
            ),
        }
    )
    public ResponseEntity<Void> deleteExpense(
        @Parameter(description = "Company ID") @PathVariable UUID companyId,
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        expenseService.deleteExpense(companyId, projectId, expenseId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{expenseId}/mark-paid")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Mark expense as paid",
        description = "Updates expense status to PAID with payment date and method."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Expense marked as paid"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error or expense already paid/cancelled"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires ADMIN or MANAGER role"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Expense not found"
            ),
        }
    )
    public ResponseEntity<ExpenseResponse> markAsPaid(
        @Parameter(description = "Company ID") @PathVariable UUID companyId,
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
        @Valid @RequestBody MarkAsPaidRequest request,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        ExpenseResponse expense = expenseService.markAsPaid(
            companyId,
            projectId,
            expenseId,
            request
        );
        return ResponseEntity.ok(expense);
    }

    @PatchMapping("/{expenseId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Cancel expense",
        description = "Cancels an expense. Cannot cancel already paid expenses."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Expense cancelled"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Cannot cancel paid expense"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires ADMIN or MANAGER role"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Expense not found"
            ),
        }
    )
    public ResponseEntity<ExpenseResponse> cancelExpense(
        @Parameter(description = "Company ID") @PathVariable UUID companyId,
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        ExpenseResponse expense = expenseService.cancelExpense(
            companyId,
            projectId,
            expenseId
        );
        return ResponseEntity.ok(expense);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "List overdue expenses",
        description = "Returns all expenses that are past their due date and not yet paid."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Overdue expenses retrieved"
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(
                responseCode = "404",
                description = "Project not found"
            ),
        }
    )
    public ResponseEntity<List<ExpenseResponse>> listOverdueExpenses(
        @Parameter(description = "Company ID") @PathVariable UUID companyId,
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        List<ExpenseResponse> expenses = expenseService.listOverdueExpenses(
            companyId,
            projectId
        );
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/by-category/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "List expenses by category",
        description = "Returns all expenses for a specific category."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Expenses retrieved"
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(
                responseCode = "404",
                description = "Project not found"
            ),
        }
    )
    public ResponseEntity<List<ExpenseResponse>> listExpensesByCategory(
        @Parameter(description = "Company ID") @PathVariable UUID companyId,
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Category ID") @PathVariable UUID categoryId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        List<ExpenseResponse> expenses = expenseService.listExpensesByCategory(
            companyId,
            projectId,
            categoryId
        );
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Get financial summary",
        description = "Returns comprehensive financial summary including total expenses, paid amounts, pending amounts, breakdown by category, and overdue count."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Financial summary retrieved",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                        implementation = FinancialSummaryResponse.class
                    )
                )
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(
                responseCode = "404",
                description = "Project not found"
            ),
        }
    )
    public ResponseEntity<FinancialSummaryResponse> getFinancialSummary(
        @Parameter(description = "Company ID") @PathVariable UUID companyId,
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        Authentication authentication
    ) {
        validateCompanyAccess(authentication, companyId);

        FinancialSummaryResponse summary = expenseService.getFinancialSummary(
            companyId,
            projectId
        );
        return ResponseEntity.ok(summary);
    }

    private void validateCompanyAccess(
        Authentication authentication,
        UUID companyId
    ) {
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();

        if (userData.isMasterCompany()) {
            return;
        }

        if (!userData.companyId().equals(companyId)) {
            throw new IllegalStateException(
                "You can only access expenses from your own company"
            );
        }
    }
}
