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
@RequestMapping("/projects/{projectId}/expenses")
@SecurityRequirement(name = "bearer-jwt")
@Tag(
    name = "Expenses",
    description = "Financial expense management for construction projects. Track costs, payments, and generate financial reports. SUPER_ADMIN can optionally specify companyId via query parameter."
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
        description = "Creates a new expense entry for the project. Supports single payments or installments. SUPER_ADMIN can optionally specify companyId via query parameter."
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
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        @Valid @RequestBody CreateExpenseRequest request,
        Authentication authentication
    ) {
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        ExpenseResponse expense = expenseService.createExpense(
            targetCompanyId,
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
        description = "Returns paginated list of all expenses for the project, ordered by due date (most recent first). SUPER_ADMIN can optionally specify companyId via query parameter."
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
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        @PageableDefault(
            size = 20,
            sort = "dueDate",
            direction = Sort.Direction.DESC
        ) Pageable pageable,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        Page<ExpenseResponse> expenses = expenseService.listProjectExpenses(
            targetCompanyId,
            projectId,
            pageable
        );
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{expenseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Get expense by ID",
        description = "Returns detailed information about a specific expense including installments if applicable. SUPER_ADMIN can optionally specify companyId via query parameter."
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
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        ExpenseResponse expense = expenseService.getExpenseById(
            targetCompanyId,
            projectId,
            expenseId
        );
        return ResponseEntity.ok(expense);
    }

    @PutMapping("/{expenseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Update expense",
        description = "Updates expense information. All fields are optional - only provided fields will be updated. SUPER_ADMIN can optionally specify companyId via query parameter."
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
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        @Valid @RequestBody UpdateExpenseRequest request,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        ExpenseResponse expense = expenseService.updateExpense(
            targetCompanyId,
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
        description = "Permanently deletes an expense and all associated installments. This action cannot be undone. SUPER_ADMIN can optionally specify companyId via query parameter."
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
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        expenseService.deleteExpense(targetCompanyId, projectId, expenseId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{expenseId}/mark-paid")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Mark expense as paid",
        description = "Updates expense status to PAID with payment date and method. SUPER_ADMIN can optionally specify companyId via query parameter."
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
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        @Valid @RequestBody MarkAsPaidRequest request,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        ExpenseResponse expense = expenseService.markAsPaid(
            targetCompanyId,
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
        description = "Cancels an expense. Cannot cancel already paid expenses. SUPER_ADMIN can optionally specify companyId via query parameter."
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
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        ExpenseResponse expense = expenseService.cancelExpense(
            targetCompanyId,
            projectId,
            expenseId
        );
        return ResponseEntity.ok(expense);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "List overdue expenses",
        description = "Returns all expenses that are past their due date and not yet paid. SUPER_ADMIN can optionally specify companyId via query parameter."
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
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        List<ExpenseResponse> expenses = expenseService.listOverdueExpenses(
            targetCompanyId,
            projectId
        );
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/by-category/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "List expenses by category",
        description = "Returns all expenses for a specific category. SUPER_ADMIN can optionally specify companyId via query parameter."
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
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Category ID") @PathVariable UUID categoryId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        List<ExpenseResponse> expenses = expenseService.listExpensesByCategory(
            targetCompanyId,
            projectId,
            categoryId
        );
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Get financial summary",
        description = "Returns comprehensive financial summary including total expenses, paid amounts, pending amounts, breakdown by category, and overdue count. SUPER_ADMIN can optionally specify companyId via query parameter."
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
        @Parameter(description = "Project ID") @PathVariable UUID projectId,
        @Parameter(description = "Company ID (optional, only for SUPER_ADMIN)")
        @RequestParam(required = false) UUID companyId,
        Authentication authentication
    ) {
        UUID targetCompanyId = getTargetCompanyId(authentication, companyId);

        FinancialSummaryResponse summary = expenseService.getFinancialSummary(
            targetCompanyId,
            projectId
        );
        return ResponseEntity.ok(summary);
    }

    private UUID getTargetCompanyId(Authentication authentication, UUID requestedCompanyId) {
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();
        
        if (requestedCompanyId != null) {
            if (!userData.isMasterCompany()) {
                throw new IllegalStateException(
                    "Only SUPER_ADMIN can access other companies' resources"
                );
            }
            return requestedCompanyId;
        }
        
        return userData.companyId();
    }
}
