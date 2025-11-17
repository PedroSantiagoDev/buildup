package com.maistech.buildup.company;

import static org.assertj.core.api.Assertions.assertThat;

import com.maistech.buildup.auth.domain.UserRepository;
import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.dto.LoginResponse;
import com.maistech.buildup.company.dto.AdminUserRequest;
import com.maistech.buildup.company.domain.*;
import com.maistech.buildup.company.dto.CompanyResponse;
import com.maistech.buildup.company.domain.*;
import com.maistech.buildup.company.dto.CreateCompanyRequest;
import com.maistech.buildup.company.domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CompanyIntegrationTest {

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
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    private String superAdminToken;

    @BeforeEach
    void setup() {
        superAdminToken = loginAsSuperAdmin();
    }

    @AfterEach
    void cleanup() {
        companyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateCompanyWithAdminUser() {
        AdminUserRequest adminUser = new AdminUserRequest(
            "John Doe",
            "john@acme.com",
            "password123"
        );

        CreateCompanyRequest request = new CreateCompanyRequest(
            "Acme Corporation",
            "12345678000100",
            "contact@acme.com",
            "+55 11 1234-5678",
            "123 Main St",
            null,
            adminUser
        );

        HttpHeaders headers = createAuthHeaders(superAdminToken);
        HttpEntity<CreateCompanyRequest> entity = new HttpEntity<>(
            request,
            headers
        );

        ResponseEntity<CompanyResponse> response = restTemplate.exchange(
            "/companies",
            HttpMethod.POST,
            entity,
            CompanyResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Acme Corporation");
        assertThat(response.getBody().document()).isEqualTo("12345678000100");

        assertThat(userRepository.findByEmail("john@acme.com")).isPresent();
    }

    @Test
    void shouldNotAllowDuplicateDocument() {
        CreateCompanyRequest request1 = new CreateCompanyRequest(
            "Company A",
            "12345678000100",
            "companya@test.com",
            null,
            null,
            null,
            null
        );

        HttpHeaders headers = createAuthHeaders(superAdminToken);
        HttpEntity<CreateCompanyRequest> entity1 = new HttpEntity<>(
            request1,
            headers
        );

        restTemplate.exchange(
            "/companies",
            HttpMethod.POST,
            entity1,
            CompanyResponse.class
        );

        CreateCompanyRequest request2 = new CreateCompanyRequest(
            "Company B",
            "12345678000100",
            "companyb@test.com",
            null,
            null,
            null,
            null
        );

        HttpEntity<CreateCompanyRequest> entity2 = new HttpEntity<>(
            request2,
            headers
        );

        ResponseEntity<CompanyResponse> response = restTemplate.exchange(
            "/companies",
            HttpMethod.POST,
            entity2,
            CompanyResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldGetCompanyById() {
        var company = createTestCompany("Test Company", "98765432000100");

        HttpHeaders headers = createAuthHeaders(superAdminToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<CompanyResponse> response = restTemplate.exchange(
            "/companies/" + company.getId(),
            HttpMethod.GET,
            entity,
            CompanyResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(company.getId());
        assertThat(response.getBody().name()).isEqualTo("Test Company");
    }

    @Test
    void shouldRequireSuperAdminToCreateCompany() {
        CreateCompanyRequest request = new CreateCompanyRequest(
            "Unauthorized Company",
            "11111111000100",
            "test@test.com",
            null,
            null,
            null,
            null
        );

        ResponseEntity<CompanyResponse> response = restTemplate.postForEntity(
            "/companies",
            request,
            CompanyResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private String loginAsSuperAdmin() {
        LoginRequest loginRequest = new LoginRequest(
            "superadmin@buildup.com",
            "admin123"
        );

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
            "/auth/login",
            loginRequest,
            LoginResponse.class
        );

        return response.getBody().token();
    }

    private CompanyEntity createTestCompany(String name, String document) {
        var masterCompany = companyRepository
            .findMasterCompany()
            .orElseThrow(() ->
                new IllegalStateException("Master company not found")
            );

        CompanyEntity company = new CompanyEntity();
        company.setName(name);
        company.setDocument(document);
        company.setEmail("test@" + document + ".com");
        company.setIsActive(true);
        company.setMasterCompany(masterCompany);

        return companyRepository.save(company);
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
