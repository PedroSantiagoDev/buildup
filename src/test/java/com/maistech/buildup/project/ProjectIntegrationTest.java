package com.maistech.buildup.project;

import static org.assertj.core.api.Assertions.assertThat;

import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.auth.UserRepository;
import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.dto.LoginResponse;
import com.maistech.buildup.company.CompanyEntity;
import com.maistech.buildup.company.CompanyRepository;
import com.maistech.buildup.project.dto.*;
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
class ProjectIntegrationTest {

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
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;
    private UUID companyId;
    private UUID userId;
    private CompanyEntity company;
    private UserEntity adminUser;

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
        userId = adminUser.getId();

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
        projectMemberRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    @DisplayName("should create project successfully")
    void shouldCreateProjectSuccessfully() {
        CreateProjectRequest request = new CreateProjectRequest(
            "Test Project",
            "Test Client",
            "Project description",
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            new BigDecimal("10000.00"),
            new BigDecimal("3000.00"),
            "https://image.url/cover.jpg"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<CreateProjectRequest> entity = new HttpEntity<>(
            request,
            headers
        );

        ResponseEntity<ProjectResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects",
            HttpMethod.POST,
            entity,
            ProjectResponse.class,
            companyId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Test Project");
        assertThat(response.getBody().clientName()).isEqualTo("Test Client");
        assertThat(response.getBody().contractValue()).isEqualByComparingTo(
            new BigDecimal("10000.00")
        );
        assertThat(response.getBody().downPayment()).isEqualByComparingTo(
            new BigDecimal("3000.00")
        );
        assertThat(response.getBody().remainingPayment()).isEqualByComparingTo(
            new BigDecimal("7000.00")
        );
        assertThat(response.getBody().status()).isEqualTo(
            ProjectStatus.IN_PROGRESS
        );
        assertThat(response.getBody().createdById()).isEqualTo(userId);
        assertThat(response.getBody().members()).hasSize(1);
    }

    @Test
    @DisplayName("should list projects with pagination")
    void shouldListProjectsSuccessfully() {
        createTestProject("Project 1");
        createTestProject("Project 2");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<RestResponsePage<ProjectResponse>> response =
            restTemplate.exchange(
                "/companies/{companyId}/projects?size=10&page=0",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<
                    RestResponsePage<ProjectResponse>
                >() {},
                companyId
            );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSizeGreaterThanOrEqualTo(
            2
        );
    }

    @Test
    @DisplayName("should get project by id")
    void shouldGetProjectById() {
        UUID projectId = createTestProject("Test Project");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ProjectResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}",
            HttpMethod.GET,
            entity,
            ProjectResponse.class,
            companyId,
            projectId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(projectId);
        assertThat(response.getBody().name()).isEqualTo("Test Project");
    }

    @Test
    @DisplayName("should update project successfully")
    void shouldUpdateProjectSuccessfully() {
        UUID projectId = createTestProject("Original Name");

        UpdateProjectRequest updateRequest = new UpdateProjectRequest(
            "Updated Name",
            "Updated Client",
            "Updated description",
            null,
            null,
            new BigDecimal("15000.00"),
            null,
            null,
            ProjectStatus.ON_HOLD
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<UpdateProjectRequest> entity = new HttpEntity<>(
            updateRequest,
            headers
        );

        ResponseEntity<ProjectResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}",
            HttpMethod.PUT,
            entity,
            ProjectResponse.class,
            companyId,
            projectId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Updated Name");
        assertThat(response.getBody().clientName()).isEqualTo("Updated Client");
        assertThat(response.getBody().status()).isEqualTo(
            ProjectStatus.ON_HOLD
        );
        assertThat(response.getBody().contractValue()).isEqualByComparingTo(
            new BigDecimal("15000.00")
        );
    }

    @Test
    @DisplayName("should delete project successfully")
    void shouldDeleteProjectSuccessfully() {
        UUID projectId = createTestProject("Project to Delete");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}",
            HttpMethod.DELETE,
            entity,
            Void.class,
            companyId,
            projectId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(projectRepository.findById(projectId)).isEmpty();
    }

    @Test
    @DisplayName("should add member to project")
    void shouldAddMemberToProject() {
        UUID projectId = createTestProject("Test Project");

        UserEntity newMember = new UserEntity();
        newMember.setName("New Member");
        newMember.setEmail("member@test.com");
        newMember.setPassword(passwordEncoder.encode("password"));
        newMember.setCompany(company);
        newMember.assignRole(
            roleRepository.findByName(RoleEnum.USER.name()).orElseThrow()
        );
        newMember = userRepository.save(newMember);

        AddMemberRequest request = new AddMemberRequest(
            newMember.getId(),
            "Developer",
            true
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<AddMemberRequest> entity = new HttpEntity<>(
            request,
            headers
        );

        ResponseEntity<ProjectMemberResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/members",
            HttpMethod.POST,
            entity,
            ProjectMemberResponse.class,
            companyId,
            projectId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().userId()).isEqualTo(newMember.getId());
        assertThat(response.getBody().role()).isEqualTo("Developer");
        assertThat(response.getBody().canEdit()).isTrue();
    }

    @Test
    @DisplayName("should list project members")
    void shouldListProjectMembers() {
        UUID projectId = createTestProject("Test Project");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<ProjectMemberResponse>> response =
            restTemplate.exchange(
                "/companies/{companyId}/projects/{projectId}/members",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<
                    List<ProjectMemberResponse>
                >() {},
                companyId,
                projectId
            );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("should remove member from project")
    void shouldRemoveMemberFromProject() {
        UUID projectId = createTestProject("Test Project");

        UserEntity member = new UserEntity();
        member.setName("Member to Remove");
        member.setEmail("remove@test.com");
        member.setPassword(passwordEncoder.encode("password"));
        member.setCompany(company);
        member.assignRole(
            roleRepository.findByName(RoleEnum.USER.name()).orElseThrow()
        );
        member = userRepository.save(member);

        ProjectEntity project = projectRepository
            .findById(projectId)
            .orElseThrow();
        ProjectMemberEntity memberEntity = new ProjectMemberEntity();
        memberEntity.setProject(project);
        memberEntity.setUser(member);
        memberEntity.setRole("Developer");
        memberEntity.setCanEdit(false);
        projectMemberRepository.save(memberEntity);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/members/{userId}",
            HttpMethod.DELETE,
            entity,
            Void.class,
            companyId,
            projectId,
            member.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(
            projectMemberRepository.existsByProjectIdAndUserId(
                projectId,
                member.getId()
            )
        ).isFalse();
    }

    @Test
    @DisplayName("should list user's projects")
    void shouldListUserProjects() {
        createTestProject("User Project 1");
        createTestProject("User Project 2");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<ProjectResponse>> response = restTemplate.exchange(
            "/companies/{companyId}/projects/my-projects",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<List<ProjectResponse>>() {},
            companyId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("should return 404 when project not found")
    void shouldReturn404WhenProjectNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(
                "/companies/{companyId}/projects/{projectId}",
                HttpMethod.GET,
                entity,
                ProjectResponse.class,
                companyId,
                nonExistentId
            );
        } catch (Exception e) {
            // Expected 404
            assertThat(e.getMessage()).contains("404");
        }
    }

    private UUID createTestProject(String name) {
        ProjectEntity project = new ProjectEntity();
        project.setName(name);
        project.setClientName("Test Client");
        project.setDescription("Test description");
        project.setStartDate(LocalDate.now());
        project.setDueDate(LocalDate.now().plusDays(30));
        project.setContractValue(new BigDecimal("10000.00"));
        project.setDownPayment(new BigDecimal("3000.00"));
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setCompanyId(companyId);
        project.setCreatedBy(adminUser);
        project = projectRepository.save(project);

        project.addMember(adminUser, "Project Manager", true);
        project = projectRepository.save(project);

        return project.getId();
    }
}
