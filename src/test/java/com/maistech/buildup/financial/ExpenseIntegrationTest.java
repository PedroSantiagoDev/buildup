package com.maistech.buildup.financial;

import static org.assertj.core.api.Assertions.assertThat;

import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.auth.UserRepository;
import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.dto.LoginResponse;
import com.maistech.buildup.company.CompanyEntity;
import com.maistech.buildup.company.CompanyRepository;
import com.maistech.buildup.financial.dto.*;
import com.maistech.buildup.project.ProjectEntity;
import com.maistech.buildup.project.ProjectRepository;
import com.maistech.buildup.project.ProjectStatus;
import com.maistech.buildup.role.RoleEntity;
import com.maistech.buildup.role.RoleEnum;
import com.maistech.buildup.role.RoleRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ExpenseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        "postgres:16-alpine"
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseCategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;
    private UUID companyId;
    private UUID projectId;
    private UUID categoryId;
    private CompanyEntity company;
    private UserEntity adminUser;
    private ProjectEntity project;

    @BeforeEach
    void setUp() {
        company = new CompanyEntity();
        company.setName("Test Company");
        company.setDocument("12345678000190");
        company.setEmail("test@company.com");
        company.setPhone("11999999999");
        company.setAddress("Test Address");
        company.setIsMaster(false);
        company.setIsActive(true);
        company = companyRepository.save(company);
        companyId = company.getId();

        RoleEntity adminRole = roleRepository
            .findByName(RoleEnum.ADMIN.name())
            .orElseThrow();

        adminUser = new UserEntity();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword(passwordEncoder.encode("password123"));
        adminUser.setCompany(company);
        adminUser.assignRole(adminRole);
        adminUser = userRepository.save(adminUser);

        project = new ProjectEntity();
        project.setName("Test Project");
        project.setClientName("Test Client");
        project.setDescription("Test description");
        project.setStartDate(LocalDate.now());
        project.setDueDate(LocalDate.now().plusDays(30));
        project.setContractValue(new BigDecimal("100000.00"));
        project.setDownPayment(new BigDecimal("30000.00"));
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setCompanyId(companyId);
        project.setCreatedBy(adminUser);
        project = projectRepository.save(project);
        projectId = project.getId();

        ExpenseCategoryEntity category = categoryRepository
            .findByName("MATERIAIS")
            .orElseThrow();
        categoryId = category.getId();

        LoginRequest loginRequest = new LoginRequest(
            "admin@test.com",
            "password123"
        );
        ResponseEntity<LoginResponse> loginResponse =
            restTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                LoginResponse.class
            );
        authToken = loginResponse.getBody().token();
    }

    @AfterEach
    void cleanup() {
        expenseRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    @DisplayName("should create expense successfully")
    void shouldCreateExpenseSuccessfully() {
        CreateExpenseRequest request = new CreateExpenseRequest(
            categoryId,
            "Cement purchase",
            new BigDecimal("5000.00"),
            LocalDate.now().plusDays(15),
            PaymentMethod.PIX,
            "Supplier ABC",
            "INV-001",
            "https://invoice.url/001.pdf",
            "Urgent delivery",
            null
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<CreateExpenseRequest> entity = new HttpEntity<>(
            request,
            headers
        );

        ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/expenses",
            HttpMethod.POST,
            entity,
            ExpenseResponse.class,
            companyId,
            projectId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().description()).isEqualTo(
            "Cement purchase"
        );
        assertThat(response.getBody().amount()).isEqualByComparingTo(
            new BigDecimal("5000.00")
        );
        assertThat(response.getBody().status()).isEqualTo(
            ExpenseStatus.PENDING
        );
        assertThat(response.getBody().supplier()).isEqualTo("Supplier ABC");
        assertThat(response.getBody().hasInstallments()).isFalse();
    }

    @Test
    @DisplayName("should create expense with installments")
    void shouldCreateExpenseWithInstallments() {
        List<InstallmentRequest> installments = List.of(
            new InstallmentRequest(
                new BigDecimal("2000.00"),
                LocalDate.now().plusDays(30)
            ),
            new InstallmentRequest(
                new BigDecimal("2000.00"),
                LocalDate.now().plusDays(60)
            ),
            new InstallmentRequest(
                new BigDecimal("2000.00"),
                LocalDate.now().plusDays(90)
            )
        );

        CreateExpenseRequest request = new CreateExpenseRequest(
            categoryId,
            "Equipment purchase in 3x",
            new BigDecimal("6000.00"),
            LocalDate.now().plusDays(30),
            null,
            "Equipment Store",
            "INV-002",
            null,
            "Payment in 3 installments",
            installments
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<CreateExpenseRequest> entity = new HttpEntity<>(
            request,
            headers
        );

        ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/expenses",
            HttpMethod.POST,
            entity,
            ExpenseResponse.class,
            companyId,
            projectId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().hasInstallments()).isTrue();
        assertThat(response.getBody().installments()).hasSize(3);
        assertThat(response.getBody().remainingAmount()).isEqualByComparingTo(
            new BigDecimal("6000.00")
        );
    }

    @Test
    @DisplayName("should list project expenses")
    void shouldListProjectExpenses() {
        createTestExpense("Expense 1", new BigDecimal("1000.00"));
        createTestExpense("Expense 2", new BigDecimal("2000.00"));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/expenses?size=10&page=0",
            HttpMethod.GET,
            entity,
            String.class,
            companyId,
            projectId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Expense 1");
        assertThat(response.getBody()).contains("Expense 2");
    }

    @Test
    @DisplayName("should get expense by id")
    void shouldGetExpenseById() {
        UUID expenseId = createTestExpense(
            "Test Expense",
            new BigDecimal("3000.00")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/expenses/{expenseId}",
            HttpMethod.GET,
            entity,
            ExpenseResponse.class,
            companyId,
            projectId,
            expenseId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(expenseId);
        assertThat(response.getBody().description()).isEqualTo("Test Expense");
    }

    @Test
    @DisplayName("should update expense")
    void shouldUpdateExpense() {
        UUID expenseId = createTestExpense(
            "Original",
            new BigDecimal("1000.00")
        );

        UpdateExpenseRequest updateRequest = new UpdateExpenseRequest(
            "Updated Description",
            new BigDecimal("1500.00"),
            LocalDate.now().plusDays(20),
            PaymentMethod.BOLETO,
            "New Supplier",
            "INV-999",
            "https://new-invoice.url",
            "Updated notes"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<UpdateExpenseRequest> entity = new HttpEntity<>(
            updateRequest,
            headers
        );

        ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/expenses/{expenseId}",
            HttpMethod.PUT,
            entity,
            ExpenseResponse.class,
            companyId,
            projectId,
            expenseId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().description()).isEqualTo(
            "Updated Description"
        );
        assertThat(response.getBody().amount()).isEqualByComparingTo(
            new BigDecimal("1500.00")
        );
        assertThat(response.getBody().supplier()).isEqualTo("New Supplier");
    }

    @Test
    @DisplayName("should mark expense as paid")
    void shouldMarkExpenseAsPaid() {
        UUID expenseId = createTestExpense("To Pay", new BigDecimal("2000.00"));

        MarkAsPaidRequest request = new MarkAsPaidRequest(
            LocalDate.now(),
            PaymentMethod.PIX
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<MarkAsPaidRequest> entity = new HttpEntity<>(
            request,
            headers
        );

        ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/expenses/{expenseId}/mark-paid",
            HttpMethod.PATCH,
            entity,
            ExpenseResponse.class,
            companyId,
            projectId,
            expenseId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(ExpenseStatus.PAID);
        assertThat(response.getBody().paidDate()).isEqualTo(LocalDate.now());
        assertThat(response.getBody().paymentMethod()).isEqualTo(
            PaymentMethod.PIX
        );
    }

    @Test
    @DisplayName("should cancel expense")
    void shouldCancelExpense() {
        UUID expenseId = createTestExpense(
            "To Cancel",
            new BigDecimal("1000.00")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ExpenseResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/expenses/{expenseId}/cancel",
            HttpMethod.PATCH,
            entity,
            ExpenseResponse.class,
            companyId,
            projectId,
            expenseId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(
            ExpenseStatus.CANCELLED
        );
    }

    @Test
    @DisplayName("should delete expense")
    void shouldDeleteExpense() {
        UUID expenseId = createTestExpense(
            "To Delete",
            new BigDecimal("500.00")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/expenses/{expenseId}",
            HttpMethod.DELETE,
            entity,
            Void.class,
            companyId,
            projectId,
            expenseId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(expenseRepository.findById(expenseId)).isEmpty();
    }

    @Test
    @DisplayName("should get financial summary")
    void shouldGetFinancialSummary() {
        createTestExpense("Expense 1", new BigDecimal("5000.00"));

        UUID expenseId2 = createTestExpense(
            "Expense 2",
            new BigDecimal("3000.00")
        );
        ExpenseEntity expense2 = expenseRepository
            .findById(expenseId2)
            .orElseThrow();
        expense2.markAsPaid(LocalDate.now(), PaymentMethod.PIX);
        expenseRepository.save(expense2);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<FinancialSummaryResponse> response =
            restTemplate.exchange(
                "/companies/{companyId}/projects/{projectId}/expenses/summary",
                HttpMethod.GET,
                entity,
                FinancialSummaryResponse.class,
                companyId,
                projectId
            );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalExpenses()).isEqualByComparingTo(
            new BigDecimal("8000.00")
        );
        assertThat(response.getBody().totalPaid()).isEqualByComparingTo(
            new BigDecimal("3000.00")
        );
        assertThat(response.getBody().totalPending()).isEqualByComparingTo(
            new BigDecimal("5000.00")
        );
    }

    @Test
    @DisplayName("should list overdue expenses")
    void shouldListOverdueExpenses() {
        ExpenseEntity overdueExpense = new ExpenseEntity();
        overdueExpense.setProject(project);
        overdueExpense.setCategory(
            categoryRepository.findById(categoryId).orElseThrow()
        );
        overdueExpense.setDescription("Overdue Expense");
        overdueExpense.setAmount(new BigDecimal("1000.00"));
        overdueExpense.setDueDate(LocalDate.now().minusDays(5));
        overdueExpense.setStatus(ExpenseStatus.PENDING);
        overdueExpense.setCreatedBy(adminUser);
        expenseRepository.save(overdueExpense);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<ExpenseResponse>> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/expenses/overdue",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<List<ExpenseResponse>>() {},
            companyId,
            projectId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(response.getBody().get(0).isOverdue()).isTrue();
    }

    @Test
    @DisplayName("should list expenses by category")
    void shouldListExpensesByCategory() {
        UUID expense1Id = createTestExpense("Material 1", new BigDecimal("500.00"));
        UUID expense2Id = createTestExpense("Material 2", new BigDecimal("750.00"));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<ExpenseResponse>> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/expenses/by-category/{categoryId}",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<List<ExpenseResponse>>() {},
            companyId,
            projectId,
            categoryId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(2);
    }

    private UUID createTestExpense(String description, BigDecimal amount) {
        ExpenseEntity expense = new ExpenseEntity();
        expense.setProject(project);
        expense.setCategory(
            categoryRepository.findById(categoryId).orElseThrow()
        );
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setDueDate(LocalDate.now().plusDays(10));
        expense.setStatus(ExpenseStatus.PENDING);
        expense.setCreatedBy(adminUser);
        expense = expenseRepository.save(expense);
        return expense.getId();
    }
}
