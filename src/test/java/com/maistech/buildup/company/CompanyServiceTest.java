package com.maistech.buildup.company;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.auth.UserRepository;
import com.maistech.buildup.company.dto.AdminUserRequest;
import com.maistech.buildup.company.dto.CompanyResponse;
import com.maistech.buildup.company.dto.CreateCompanyRequest;
import com.maistech.buildup.company.dto.UpdateCompanyRequest;
import com.maistech.buildup.role.RoleEntity;
import com.maistech.buildup.role.RoleEnum;
import com.maistech.buildup.role.RoleRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CompanyService companyService;

    private UUID masterCompanyId;
    private CompanyEntity masterCompany;
    private CreateCompanyRequest validRequest;
    private AdminUserRequest adminUserRequest;

    @BeforeEach
    void setUp() {
        masterCompanyId = UUID.randomUUID();
        masterCompany = new CompanyEntity();
        masterCompany.setId(masterCompanyId);
        masterCompany.setName("Master Company");
        masterCompany.setDocument("12345678000190");
        masterCompany.setEmail("master@company.com");
        masterCompany.setIsMaster(true);
        masterCompany.setIsActive(true);

        adminUserRequest = new AdminUserRequest(
            "Admin User",
            "admin@client.com",
            "password123"
        );

        validRequest = new CreateCompanyRequest(
            "Client Company",
            "98765432000111",
            "client@company.com",
            "11999999999",
            "Rua Teste, 123",
            "https://logo.url",
            adminUserRequest
        );
    }

    @Test
    @DisplayName("companyIsCreatedSuccessfully")
    void companyIsCreatedSuccessfully() {
        when(
            companyRepository.existsByDocument(validRequest.document())
        ).thenReturn(false);
        when(companyRepository.findById(masterCompanyId)).thenReturn(
            Optional.of(masterCompany)
        );
        when(companyRepository.save(any(CompanyEntity.class))).thenAnswer(
            invocation -> {
                CompanyEntity company = invocation.getArgument(0);
                company.setId(UUID.randomUUID());
                return company;
            }
        );
        when(userRepository.existsByEmail(adminUserRequest.email())).thenReturn(
            false
        );
        when(passwordEncoder.encode(anyString())).thenReturn(
            "encoded_password"
        );

        RoleEntity adminRole = new RoleEntity();
        adminRole.setName(RoleEnum.ADMIN.name());
        when(roleRepository.findByName(RoleEnum.ADMIN.name())).thenReturn(
            Optional.of(adminRole)
        );
        when(userRepository.save(any(UserEntity.class))).thenAnswer(
            invocation -> invocation.getArgument(0)
        );

        CompanyResponse response = companyService.createCompany(
            validRequest,
            masterCompanyId
        );

        assertNotNull(response);
        assertEquals(validRequest.name(), response.name());
        assertEquals(validRequest.document(), response.document());
        assertEquals(validRequest.email(), response.email());
        assertTrue(response.isActive());

        verify(companyRepository).existsByDocument(validRequest.document());
        verify(companyRepository).findById(masterCompanyId);
        verify(companyRepository).save(any(CompanyEntity.class));
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("companyCreationThrowsExceptionWhenDocumentAlreadyExists")
    void companyCreationThrowsExceptionWhenDocumentAlreadyExists() {
        when(
            companyRepository.existsByDocument(validRequest.document())
        ).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> companyService.createCompany(validRequest, masterCompanyId)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(companyRepository).existsByDocument(validRequest.document());
        verify(companyRepository, never()).save(any());
    }

    @Test
    @DisplayName("companyCreationThrowsExceptionWhenMasterCompanyNotFound")
    void companyCreationThrowsExceptionWhenMasterCompanyNotFound() {
        when(
            companyRepository.existsByDocument(validRequest.document())
        ).thenReturn(false);
        when(companyRepository.findById(masterCompanyId)).thenReturn(
            Optional.empty()
        );

        assertThrows(CompanyNotFoundException.class, () ->
            companyService.createCompany(validRequest, masterCompanyId)
        );

        verify(companyRepository, never()).save(any());
    }

    @Test
    @DisplayName("companyIsUpdatedSuccessfully")
    void companyIsUpdatedSuccessfully() {
        UUID companyId = UUID.randomUUID();
        CompanyEntity existingCompany = new CompanyEntity();
        existingCompany.setId(companyId);
        existingCompany.setName("Old Name");
        existingCompany.setEmail("old@email.com");

        UpdateCompanyRequest updateRequest = new UpdateCompanyRequest(
            "New Name",
            "new@email.com",
            "11988888888",
            "New Address",
            "https://new-logo.url"
        );

        when(companyRepository.findById(companyId)).thenReturn(
            Optional.of(existingCompany)
        );
        when(companyRepository.save(any(CompanyEntity.class))).thenAnswer(
            invocation -> invocation.getArgument(0)
        );

        CompanyResponse response = companyService.updateCompany(
            companyId,
            updateRequest
        );

        assertNotNull(response);
        assertEquals(updateRequest.name(), response.name());
        assertEquals(updateRequest.email(), response.email());
        verify(companyRepository).save(existingCompany);
    }

    @Test
    @DisplayName("companyIsDeactivatedSuccessfully")
    void companyIsDeactivatedSuccessfully() {
        UUID companyId = UUID.randomUUID();
        CompanyEntity company = new CompanyEntity();
        company.setId(companyId);
        company.setIsActive(true);

        when(companyRepository.findById(companyId)).thenReturn(
            Optional.of(company)
        );
        when(companyRepository.save(any(CompanyEntity.class))).thenReturn(
            company
        );

        companyService.deactivateCompany(companyId);

        assertFalse(company.getIsActive());
        verify(companyRepository).save(company);
    }

    @Test
    @DisplayName("companyIsActivatedSuccessfully")
    void companyIsActivatedSuccessfully() {
        UUID companyId = UUID.randomUUID();
        CompanyEntity company = new CompanyEntity();
        company.setId(companyId);
        company.setIsActive(false);

        when(companyRepository.findById(companyId)).thenReturn(
            Optional.of(company)
        );
        when(companyRepository.save(any(CompanyEntity.class))).thenReturn(
            company
        );

        companyService.activateCompany(companyId);

        assertTrue(company.getIsActive());
        verify(companyRepository).save(company);
    }

    @Test
    @DisplayName("companyIsRetrievedByIdSuccessfully")
    void companyIsRetrievedByIdSuccessfully() {
        UUID companyId = UUID.randomUUID();
        CompanyEntity company = new CompanyEntity();
        company.setId(companyId);
        company.setName("Test Company");
        company.setDocument("12345678000190");
        company.setEmail("test@company.com");
        company.setIsActive(true);

        when(companyRepository.findById(companyId)).thenReturn(
            Optional.of(company)
        );

        CompanyResponse response = companyService.getCompanyById(companyId);

        assertNotNull(response);
        assertEquals(company.getName(), response.name());
        assertEquals(company.getDocument(), response.document());
        verify(companyRepository).findById(companyId);
    }

    @Test
    @DisplayName("getCompanyByIdThrowsExceptionWhenNotFound")
    void getCompanyByIdThrowsExceptionWhenNotFound() {
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(
            Optional.empty()
        );

        assertThrows(CompanyNotFoundException.class, () ->
            companyService.getCompanyById(companyId)
        );
    }

    @Test
    @DisplayName("allCompaniesAreListedSuccessfully")
    void allCompaniesAreListedSuccessfully() {
        CompanyEntity company1 = new CompanyEntity();
        company1.setId(UUID.randomUUID());
        company1.setName("Company 1");
        company1.setDocument("11111111000111");
        company1.setEmail("company1@test.com");
        company1.setIsActive(true);

        CompanyEntity company2 = new CompanyEntity();
        company2.setId(UUID.randomUUID());
        company2.setName("Company 2");
        company2.setDocument("22222222000122");
        company2.setEmail("company2@test.com");
        company2.setIsActive(true);

        when(companyRepository.findAll()).thenReturn(
            List.of(company1, company2)
        );

        List<CompanyResponse> responses = companyService.listAllCompanies();

        assertEquals(2, responses.size());
        assertEquals("Company 1", responses.get(0).name());
        assertEquals("Company 2", responses.get(1).name());
        verify(companyRepository).findAll();
    }

    @Test
    @DisplayName("allCompaniesAreListedWithPaginationSuccessfully")
    void allCompaniesAreListedWithPaginationSuccessfully() {
        CompanyEntity company = new CompanyEntity();
        company.setId(UUID.randomUUID());
        company.setName("Company 1");
        company.setDocument("11111111000111");
        company.setEmail("company1@test.com");
        company.setIsActive(true);

        Pageable pageable = PageRequest.of(0, 10);
        Page<CompanyEntity> page = new PageImpl<>(List.of(company));

        when(companyRepository.findAll(pageable)).thenReturn(page);

        Page<CompanyResponse> responses = companyService.listAllCompanies(
            pageable
        );

        assertEquals(1, responses.getTotalElements());
        assertEquals("Company 1", responses.getContent().get(0).name());
        verify(companyRepository).findAll(pageable);
    }

    @Test
    @DisplayName("clientCompaniesAreListedSuccessfully")
    void clientCompaniesAreListedSuccessfully() {
        CompanyEntity client1 = new CompanyEntity();
        client1.setId(UUID.randomUUID());
        client1.setName("Client 1");
        client1.setDocument("11111111000111");
        client1.setEmail("client1@test.com");
        client1.setMasterCompany(masterCompany);
        client1.setIsActive(true);

        CompanyEntity client2 = new CompanyEntity();
        client2.setId(UUID.randomUUID());
        client2.setName("Client 2");
        client2.setDocument("22222222000122");
        client2.setEmail("client2@test.com");
        client2.setMasterCompany(masterCompany);
        client2.setIsActive(true);

        when(
            companyRepository.findAllClientCompanies(masterCompanyId)
        ).thenReturn(List.of(client1, client2));

        List<CompanyResponse> responses = companyService.listClientCompanies(
            masterCompanyId
        );

        assertEquals(2, responses.size());
        assertEquals("Client 1", responses.get(0).name());
        assertEquals("Client 2", responses.get(1).name());
        verify(companyRepository).findAllClientCompanies(masterCompanyId);
    }

    @Test
    @DisplayName("companyIsCreatedWithoutAdminUserSuccessfully")
    void companyIsCreatedWithoutAdminUserSuccessfully() {
        CreateCompanyRequest requestNoAdmin = new CreateCompanyRequest(
            "Client Company",
            "98765432000111",
            "client@company.com",
            "11999999999",
            "Rua Teste, 123",
            "https://logo.url",
            null
        );

        when(
            companyRepository.existsByDocument(requestNoAdmin.document())
        ).thenReturn(false);
        when(companyRepository.findById(masterCompanyId)).thenReturn(
            Optional.of(masterCompany)
        );
        when(companyRepository.save(any(CompanyEntity.class))).thenAnswer(
            invocation -> {
                CompanyEntity company = invocation.getArgument(0);
                company.setId(UUID.randomUUID());
                return company;
            }
        );

        CompanyResponse response = companyService.createCompany(
            requestNoAdmin,
            masterCompanyId
        );

        assertNotNull(response);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("adminUserCreationThrowsExceptionWhenEmailAlreadyExists")
    void adminUserCreationThrowsExceptionWhenEmailAlreadyExists() {
        when(
            companyRepository.existsByDocument(validRequest.document())
        ).thenReturn(false);
        when(companyRepository.findById(masterCompanyId)).thenReturn(
            Optional.of(masterCompany)
        );
        when(companyRepository.save(any(CompanyEntity.class))).thenAnswer(
            invocation -> {
                CompanyEntity company = invocation.getArgument(0);
                company.setId(UUID.randomUUID());
                return company;
            }
        );
        when(userRepository.existsByEmail(adminUserRequest.email())).thenReturn(
            true
        );

        assertThrows(IllegalArgumentException.class, () ->
            companyService.createCompany(validRequest, masterCompanyId)
        );

        verify(userRepository, never()).save(any());
    }
}
