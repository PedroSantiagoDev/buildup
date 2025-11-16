package com.maistech.buildup.task;

import static org.assertj.core.api.Assertions.assertThat;

import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.auth.UserRepository;
import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.dto.LoginResponse;
import com.maistech.buildup.company.CompanyEntity;
import com.maistech.buildup.company.CompanyRepository;
import com.maistech.buildup.project.ProjectEntity;
import com.maistech.buildup.project.ProjectRepository;
import com.maistech.buildup.project.ProjectStatus;
import com.maistech.buildup.role.RoleEntity;
import com.maistech.buildup.role.RoleEnum;
import com.maistech.buildup.role.RoleRepository;
import com.maistech.buildup.task.dto.*;
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
class TaskIntegrationTest {

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
    private TaskRepository taskRepository;

    @Autowired
    private TaskDependencyRepository taskDependencyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;
    private UUID companyId;
    private UUID userId;
    private UUID projectId;
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
        userId = adminUser.getId();

        project = new ProjectEntity();
        project.setName("Test Project");
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
        projectId = project.getId();

        LoginRequest loginRequest = new LoginRequest(
            "admin@test.com",
            "password123"
        );
        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
            "/auth/login",
            loginRequest,
            LoginResponse.class
        );
        authToken = loginResponse.getBody().token();
    }

    @AfterEach
    void cleanup() {
        taskDependencyRepository.deleteAll();
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    @DisplayName("should create task successfully")
    void shouldCreateTaskSuccessfully() {
        CreateTaskRequest request = new CreateTaskRequest(
            "Implement Feature X",
            "Develop the new feature X with tests",
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            Integer.valueOf(7),
            TaskPriority.HIGH,
            userId,
            Integer.valueOf(0)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<CreateTaskRequest> entity = new HttpEntity<>(
            request,
            headers
        );

        ResponseEntity<TaskResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/tasks",
            HttpMethod.POST,
            entity,
            TaskResponse.class,
            companyId,
            projectId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Implement Feature X");
        assertThat(response.getBody().priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.getBody().status()).isEqualTo(TaskStatus.PENDING);
        assertThat(response.getBody().progressPercentage()).isEqualTo(0);
    }

    @Test
    @DisplayName("should list project tasks")
    void shouldListProjectTasks() {
        createTestTask("Task 1");
        createTestTask("Task 2");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<TaskResponse>> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/tasks",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<List<TaskResponse>>() {},
            companyId,
            projectId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("should get task by id")
    void shouldGetTaskById() {
        UUID taskId = createTestTask("Test Task");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<TaskResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/tasks/{taskId}",
            HttpMethod.GET,
            entity,
            TaskResponse.class,
            companyId,
            projectId,
            taskId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(taskId);
        assertThat(response.getBody().name()).isEqualTo("Test Task");
    }

    @Test
    @DisplayName("should update task successfully")
    void shouldUpdateTaskSuccessfully() {
        UUID taskId = createTestTask("Original Task");

        UpdateTaskRequest updateRequest = new UpdateTaskRequest(
            "Updated Task",
            "Updated description",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(10),
            9,
            TaskStatus.IN_PROGRESS,
            TaskPriority.LOW,
            userId,
            50
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<UpdateTaskRequest> entity = new HttpEntity<>(
            updateRequest,
            headers
        );

        ResponseEntity<TaskResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/tasks/{taskId}",
            HttpMethod.PUT,
            entity,
            TaskResponse.class,
            companyId,
            projectId,
            taskId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Updated Task");
        assertThat(response.getBody().status()).isEqualTo(
            TaskStatus.IN_PROGRESS
        );
        assertThat(response.getBody().priority()).isEqualTo(TaskPriority.LOW);
        assertThat(response.getBody().progressPercentage()).isEqualTo(50);
    }

    @Test
    @DisplayName("should delete task successfully")
    void shouldDeleteTaskSuccessfully() {
        UUID taskId = createTestTask("Task to Delete");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/tasks/{taskId}",
            HttpMethod.DELETE,
            entity,
            Void.class,
            companyId,
            projectId,
            taskId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(taskRepository.findById(taskId)).isEmpty();
    }

    @Test
    @DisplayName("should start task")
    void shouldStartTask() {
        UUID taskId = createTestTask("Task to Start");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<TaskResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/tasks/{taskId}/start",
            HttpMethod.POST,
            entity,
            TaskResponse.class,
            companyId,
            projectId,
            taskId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(
            TaskStatus.IN_PROGRESS
        );
    }

    @Test
    @DisplayName("should complete task")
    void shouldCompleteTask() {
        UUID taskId = createTestTask("Task to Complete");

        // First start the task
        TaskEntity task = taskRepository.findById(taskId).orElseThrow();
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<TaskResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/tasks/{taskId}/complete",
            HttpMethod.POST,
            entity,
            TaskResponse.class,
            companyId,
            projectId,
            taskId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(response.getBody().progressPercentage()).isEqualTo(100);
    }

    @Test
    @DisplayName("should update task progress")
    void shouldUpdateTaskProgress() {
        UUID taskId = createTestTask("Task for Progress");

        UpdateProgressRequest progressRequest = new UpdateProgressRequest(
            Integer.valueOf(75)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<UpdateProgressRequest> entity = new HttpEntity<>(
            progressRequest,
            headers
        );

        ResponseEntity<TaskResponse> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/tasks/{taskId}/progress",
            HttpMethod.PUT,
            entity,
            TaskResponse.class,
            companyId,
            projectId,
            taskId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().progressPercentage()).isEqualTo(75);
    }

    @Test
    @DisplayName("should add task dependency")
    void shouldAddTaskDependency() {
        UUID task1Id = createTestTask("Task 1");
        UUID task2Id = createTestTask("Task 2");

        AddDependencyRequest request = new AddDependencyRequest(
            task2Id,
            DependencyType.FINISH_TO_START
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<AddDependencyRequest> entity = new HttpEntity<>(
            request,
            headers
        );

        ResponseEntity<Void> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/tasks/{taskId}/dependencies",
            HttpMethod.POST,
            entity,
            Void.class,
            companyId,
            projectId,
            task1Id
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Verify dependency was created
        List<TaskDependencyEntity> dependencies = taskDependencyRepository.findByTaskId(
            task1Id
        );
        assertThat(dependencies).hasSize(1);
        assertThat(dependencies.get(0).getDependsOnTask().getId()).isEqualTo(
            task2Id
        );
        assertThat(dependencies.get(0).getDependencyType()).isEqualTo(
            DependencyType.FINISH_TO_START
        );
    }

    @Test
    @DisplayName("should list overdue tasks")
    void shouldListOverdueTasks() {
        // Create overdue task
        TaskEntity overdueTask = new TaskEntity();
        overdueTask.setProject(project);
        overdueTask.setName("Overdue Task");
        overdueTask.setDescription("This task is overdue");
        overdueTask.setStartDate(LocalDate.now().minusDays(10));
        overdueTask.setEndDate(LocalDate.now().minusDays(1));
        overdueTask.setStatus(TaskStatus.IN_PROGRESS);
        overdueTask.setPriority(TaskPriority.HIGH);
        overdueTask.setProgressPercentage(50);
        overdueTask.setCreatedBy(adminUser);
        taskRepository.save(overdueTask);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<TaskResponse>> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/tasks/overdue",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<List<TaskResponse>>() {},
            companyId,
            projectId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(response.getBody().get(0).isOverdue()).isTrue();
    }

    @Test
    @DisplayName("should list user's assigned tasks")
    void shouldListMyTasks() {
        // Create task assigned to user
        TaskEntity assignedTask = new TaskEntity();
        assignedTask.setProject(project);
        assignedTask.setName("My Task");
        assignedTask.setDescription("Task assigned to me");
        assignedTask.setStartDate(LocalDate.now());
        assignedTask.setEndDate(LocalDate.now().plusDays(5));
        assignedTask.setStatus(TaskStatus.PENDING);
        assignedTask.setPriority(TaskPriority.MEDIUM);
        assignedTask.setProgressPercentage(0);
        assignedTask.setCreatedBy(adminUser);
        assignedTask.setAssignedTo(adminUser);
        taskRepository.save(assignedTask);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<TaskResponse>> response = restTemplate.exchange(
            "/companies/{companyId}/projects/{projectId}/tasks/my-tasks",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<List<TaskResponse>>() {},
            companyId,
            projectId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(1);
    }

    private UUID createTestTask(String name) {
        TaskEntity task = new TaskEntity();
        task.setProject(project);
        task.setName(name);
        task.setDescription("Test description");
        task.setStartDate(LocalDate.now());
        task.setEndDate(LocalDate.now().plusDays(7));
        task.setDurationDays(7);
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(TaskPriority.MEDIUM);
        task.setProgressPercentage(0);
        task.setCreatedBy(adminUser);
        task = taskRepository.save(task);
        return task.getId();
    }
}
