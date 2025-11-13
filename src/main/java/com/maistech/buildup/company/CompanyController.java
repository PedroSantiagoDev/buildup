package com.maistech.buildup.company;

import com.maistech.buildup.company.dto.CompanyResponse;
import com.maistech.buildup.company.dto.CreateCompanyRequest;
import com.maistech.buildup.company.dto.UpdateCompanyRequest;
import com.maistech.buildup.shared.config.JTWUserData;
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
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CompanyResponse> createCompany(
        @Valid @RequestBody CreateCompanyRequest request,
        Authentication authentication
    ) {
        JTWUserData userData = (JTWUserData) authentication.getPrincipal();
        CompanyResponse response = companyService.createCompany(
            request,
            userData.companyId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Page<CompanyResponse>> listCompanies(
        @PageableDefault(
            size = 20,
            sort = "createdAt",
            direction = Sort.Direction.DESC
        ) Pageable pageable,
        Authentication authentication
    ) {
        JTWUserData userData = (JTWUserData) authentication.getPrincipal();

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
    public ResponseEntity<CompanyResponse> getCompany(
        @PathVariable UUID companyId,
        Authentication authentication
    ) {
        JTWUserData userData = (JTWUserData) authentication.getPrincipal();
        validateCompanyAccess(userData, companyId);

        CompanyResponse company = companyService.getCompanyById(companyId);
        return ResponseEntity.ok(company);
    }

    @PutMapping("/{companyId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<CompanyResponse> updateCompany(
        @PathVariable UUID companyId,
        @Valid @RequestBody UpdateCompanyRequest request,
        Authentication authentication
    ) {
        JTWUserData userData = (JTWUserData) authentication.getPrincipal();
        validateCompanyAccess(userData, companyId);

        CompanyResponse company = companyService.updateCompany(
            companyId,
            request
        );
        return ResponseEntity.ok(company);
    }

    @PatchMapping("/{companyId}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deactivateCompany(
        @PathVariable UUID companyId
    ) {
        companyService.deactivateCompany(companyId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{companyId}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> activateCompany(@PathVariable UUID companyId) {
        companyService.activateCompany(companyId);
        return ResponseEntity.noContent().build();
    }

    private void validateCompanyAccess(JTWUserData userData, UUID companyId) {
        if (userData.isMasterCompany()) {
            return;
        }

        if (!userData.companyId().equals(companyId)) {
            throw new IllegalStateException(
                "You can only access your own company"
            );
        }
    }
}
