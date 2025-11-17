package com.maistech.buildup.company;

import com.maistech.buildup.company.dto.CompanyResponse;
import com.maistech.buildup.company.domain.CompanyService;
import com.maistech.buildup.company.dto.CreateCompanyRequest;
import com.maistech.buildup.company.dto.UpdateCompanyRequest;
import com.maistech.buildup.shared.config.JWTUserData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/companies")
@SecurityRequirement(name = "bearer-jwt")
@Tag(
    name = "Companies",
    description = "Company management endpoints. SUPER_ADMIN can manage all companies, ADMIN can manage their own."
)
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
        summary = "Create new company",
        description = "Creates a new company with optional admin user. Only SUPER_ADMIN can create companies."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Company created successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CompanyResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error or company already exists",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires SUPER_ADMIN role"
            ),
        }
    )
    public ResponseEntity<CompanyResponse> createCompany(
        @Valid @RequestBody CreateCompanyRequest request,
        Authentication authentication
    ) {
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();
        CompanyResponse response = companyService.createCompany(
            request,
            userData.companyId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
        summary = "List all companies",
        description = "Returns paginated list of companies. Only SUPER_ADMIN from master company can access."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Companies retrieved successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires SUPER_ADMIN from master company"
            ),
        }
    )
    public ResponseEntity<Page<CompanyResponse>> listCompanies(
        @PageableDefault(
            size = 20,
            sort = "createdAt",
            direction = Sort.Direction.DESC
        ) @Parameter(description = "Pagination parameters") Pageable pageable,
        Authentication authentication
    ) {
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();

        Page<CompanyResponse> companies;
        if (userData.isMasterCompany()) {
            companies = companyService.listAllCompanies(pageable);
        } else {
            throw new IllegalStateException(
                "Only SUPER_ADMIN from master company can list all companies"
            );
        }

        return ResponseEntity.ok(companies);
    }

    @GetMapping("/{companyId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
        summary = "Get company details",
        description = "Returns company information. ADMIN can only view their own company, SUPER_ADMIN can view any."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Company found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CompanyResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - cannot access other company"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Company not found"
            ),
        }
    )
    public ResponseEntity<CompanyResponse> getCompany(
        @PathVariable @Parameter(description = "Company UUID") UUID companyId,
        Authentication authentication
    ) {
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();
        CompanyResponse company = companyService.getCompanyById(
            companyId,
            userData.companyId(),
            userData.isMasterCompany()
        );
        return ResponseEntity.ok(company);
    }

    @PutMapping("/{companyId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(
        summary = "Update company",
        description = "Updates company information. ADMIN can only update their own company, SUPER_ADMIN can update any."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Company updated successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - cannot update other company"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Company not found"
            ),
        }
    )
    public ResponseEntity<CompanyResponse> updateCompany(
        @PathVariable @Parameter(description = "Company UUID") UUID companyId,
        @Valid @RequestBody UpdateCompanyRequest request,
        Authentication authentication
    ) {
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();
        CompanyResponse company = companyService.updateCompany(
            companyId,
            request,
            userData.companyId(),
            userData.isMasterCompany()
        );
        return ResponseEntity.ok(company);
    }

    @PatchMapping("/{companyId}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
        summary = "Deactivate company",
        description = "Deactivates a company. Only SUPER_ADMIN can perform this action."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "204",
                description = "Company deactivated"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires SUPER_ADMIN"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Company not found"
            ),
        }
    )
    public ResponseEntity<Void> deactivateCompany(
        @PathVariable @Parameter(description = "Company UUID") UUID companyId
    ) {
        companyService.deactivateCompany(companyId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{companyId}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
        summary = "Activate company",
        description = "Activates a company. Only SUPER_ADMIN can perform this action."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "204",
                description = "Company activated"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires SUPER_ADMIN"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Company not found"
            ),
        }
    )
    public ResponseEntity<Void> activateCompany(
        @PathVariable @Parameter(description = "Company UUID") UUID companyId
    ) {
        companyService.activateCompany(companyId);
        return ResponseEntity.noContent().build();
    }
}
