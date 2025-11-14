package com.maistech.buildup.company;

import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.auth.UserRepository;
import com.maistech.buildup.company.dto.AdminUserRequest;
import com.maistech.buildup.company.dto.CompanyResponse;
import com.maistech.buildup.company.dto.CreateCompanyRequest;
import com.maistech.buildup.company.dto.UpdateCompanyRequest;
import com.maistech.buildup.role.RoleEntity;
import com.maistech.buildup.role.RoleEnum;
import com.maistech.buildup.role.RoleRepository;
import com.maistech.buildup.shared.tenant.TenantHelper;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantHelper tenantHelper;

    public CompanyService(
        CompanyRepository companyRepository,
        UserRepository userRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder,
        TenantHelper tenantHelper
    ) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tenantHelper = tenantHelper;
    }

    public CompanyResponse createCompany(
        CreateCompanyRequest request,
        UUID masterCompanyId
    ) {
        return tenantHelper.withoutTenantFilter(() -> {
            validateDocumentDoesNotExist(request.document());

            CompanyEntity masterCompany = companyRepository
                .findById(masterCompanyId)
                .orElseThrow(() ->
                    new CompanyNotFoundException("Master company not found")
                );

            CompanyEntity company = new CompanyEntity();
            company.setName(request.name());
            company.setDocument(request.document());
            company.setEmail(request.email());
            company.setPhone(request.phone());
            company.setAddress(request.address());
            company.setLogoUrl(request.logoUrl());
            company.setIsMaster(false);
            company.setIsActive(true);
            company.setMasterCompany(masterCompany);

            company = companyRepository.save(company);

            if (request.adminUser() != null) {
                createAdminUser(company, request.adminUser());
            }

            return mapToResponse(company);
        });
    }

    public CompanyResponse updateCompany(
        UUID companyId,
        UpdateCompanyRequest request
    ) {
        return tenantHelper.withoutTenantFilter(() -> {
            CompanyEntity company = findCompanyOrThrow(companyId);

            if (request.name() != null) {
                company.setName(request.name());
            }
            if (request.email() != null) {
                company.setEmail(request.email());
            }
            if (request.phone() != null) {
                company.setPhone(request.phone());
            }
            if (request.address() != null) {
                company.setAddress(request.address());
            }
            if (request.logoUrl() != null) {
                company.setLogoUrl(request.logoUrl());
            }

            company = companyRepository.save(company);
            return mapToResponse(company);
        });
    }

    public void deactivateCompany(UUID companyId) {
        tenantHelper.withoutTenantFilter(() -> {
            CompanyEntity company = findCompanyOrThrow(companyId);
            company.setIsActive(false);
            companyRepository.save(company);
        });
    }

    public void activateCompany(UUID companyId) {
        tenantHelper.withoutTenantFilter(() -> {
            CompanyEntity company = findCompanyOrThrow(companyId);
            company.setIsActive(true);
            companyRepository.save(company);
        });
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(UUID companyId) {
        return tenantHelper.withoutTenantFilter(() -> {
            CompanyEntity company = findCompanyOrThrow(companyId);
            return mapToResponse(company);
        });
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> listAllCompanies() {
        return tenantHelper.withoutTenantFilter(() ->
            companyRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> listAllCompanies(Pageable pageable) {
        return tenantHelper.withoutTenantFilter(() ->
            companyRepository.findAll(pageable).map(this::mapToResponse)
        );
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> listClientCompanies(UUID masterCompanyId) {
        return tenantHelper.withoutTenantFilter(() ->
            companyRepository
                .findAllClientCompanies(masterCompanyId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList())
        );
    }

    private UserEntity createAdminUser(
        CompanyEntity company,
        AdminUserRequest adminRequest
    ) {
        validateUserEmailDoesNotExist(adminRequest.email());

        UserEntity admin = new UserEntity();
        admin.setName(adminRequest.name());
        admin.setEmail(adminRequest.email());
        admin.setPassword(passwordEncoder.encode(adminRequest.password()));
        admin.setCompany(company);
        admin.setIsActive(true);

        RoleEntity adminRole = roleRepository
            .findByName(RoleEnum.ADMIN.name())
            .orElseThrow(() ->
                new IllegalStateException("ADMIN role not found")
            );
        admin.getRoles().add(adminRole);

        return userRepository.save(admin);
    }

    private void validateDocumentDoesNotExist(String document) {
        if (companyRepository.existsByDocument(document)) {
            throw new IllegalArgumentException(
                "Company with document " + document + " already exists"
            );
        }
    }

    private void validateUserEmailDoesNotExist(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                "User with email " + email + " already exists"
            );
        }
    }

    private CompanyEntity findCompanyOrThrow(UUID companyId) {
        return companyRepository
            .findById(companyId)
            .orElseThrow(() ->
                new CompanyNotFoundException("Company not found: " + companyId)
            );
    }

    private CompanyResponse mapToResponse(CompanyEntity company) {
        return new CompanyResponse(
            company.getId(),
            company.getName(),
            company.getDocument(),
            company.getEmail(),
            company.getPhone(),
            company.getAddress(),
            company.getLogoUrl(),
            company.getIsActive(),
            company.getCreatedAt()
        );
    }
}
