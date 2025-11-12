# 🏗️ BUILDUP - Guia de Implementação Tomato Architecture

## 📋 Índice
1. [Visão Geral](#visão-geral)
2. [Estrutura de Pastas](#estrutura-de-pastas)
3. [Migrations (Flyway)](#migrations-flyway)
4. [Implementação por Feature](#implementação-por-feature)
5. [Ordem de Implementação](#ordem-de-implementação)
6. [Checklist de Qualidade](#checklist-de-qualidade)

---

## 🎯 Visão Geral

O BUILDUP será implementado seguindo **Tomato Architecture** com:
- ✅ Package-by-Feature
- ✅ Constructor Injection
- ✅ No unnecessary interfaces
- ✅ Integration tests com Testcontainers
- ✅ Migrations com Flyway (UUID)

---

## 📁 Estrutura de Pastas

```
src/main/java/com/maistech/buildup/
│
├── 📦 auth/                          ✅ JÁ IMPLEMENTADO
│   ├── AuthController.java
│   ├── AuthExceptionHandler.java
│   ├── LoginUseCase.java
│   ├── RegisterUserUseCase.java
│   ├── UserEntity.java
│   ├── UserRepository.java
│   ├── UserAlreadyExistsException.java
│   └── dto/
│       ├── LoginRequest.java
│       ├── LoginResponse.java
│       ├── RegisterUserRequest.java
│       └── RegisterUserResponse.java
│
├── 📦 project/                       🔜 PRÓXIMA FEATURE
│   ├── ProjectController.java
│   ├── ProjectExceptionHandler.java
│   ├── CreateProjectUseCase.java
│   ├── UpdateProjectUseCase.java
│   ├── GetProjectDetailsUseCase.java
│   ├── AddParticipantUseCase.java
│   ├── ProjectEntity.java
│   ├── ProjectParticipantEntity.java
│   ├── ProjectRepository.java
│   ├── ProjectParticipantRepository.java
│   ├── ProjectNotFoundException.java
│   ├── InsufficientPermissionException.java
│   └── dto/
│       ├── CreateProjectRequest.java
│       ├── ProjectResponse.java
│       ├── AddParticipantRequest.java
│       └── ParticipantResponse.java
│
├── 📦 task/                          🔜 CRONOGRAMA
│   ├── TaskController.java
│   ├── TaskExceptionHandler.java
│   ├── CreateTaskUseCase.java
│   ├── UpdateTaskStatusUseCase.java
│   ├── AddTaskDependencyUseCase.java
│   ├── TaskEntity.java
│   ├── TaskDependencyEntity.java
│   ├── TaskRepository.java
│   ├── TaskDependencyRepository.java
│   ├── TaskNotFoundException.java
│   ├── CircularDependencyException.java
│   └── dto/
│       ├── CreateTaskRequest.java
│       ├── TaskResponse.java
│       └── GanttChartResponse.java
│
├── 📦 financial/                     🔜 GESTÃO FINANCEIRA
│   ├── FinancialController.java
│   ├── FinancialExceptionHandler.java
│   ├── CreatePaymentMilestoneUseCase.java
│   ├── MarkPaymentAsPaidUseCase.java
│   ├── CalculateFinancialSummaryUseCase.java
│   ├── PaymentMilestoneEntity.java
│   ├── FinancialObservationEntity.java
│   ├── PaymentMilestoneRepository.java
│   ├── FinancialObservationRepository.java
│   └── dto/
│       ├── CreateMilestoneRequest.java
│       ├── MilestoneResponse.java
│       └── FinancialSummaryResponse.java
│
├── 📦 dailylog/                      🔜 DIÁRIO DE OBRA
│   ├── DailyLogController.java
│   ├── DailyLogExceptionHandler.java
│   ├── CreateDailyLogUseCase.java
│   ├── SignDailyLogUseCase.java
│   ├── ExportDailyLogPdfUseCase.java
│   ├── DailyLogEntity.java
│   ├── DailyLogStaffEntity.java
│   ├── DailyLogRepository.java
│   ├── DailyLogAlreadyExistsException.java
│   └── dto/
│       ├── CreateDailyLogRequest.java
│       ├── DailyLogResponse.java
│       └── StaffInput.java
│
├── 📦 chat/                          🔜 CHAT
│   ├── ChatController.java
│   ├── ChatWebSocketHandler.java
│   ├── SendMessageUseCase.java
│   ├── UploadAttachmentUseCase.java
│   ├── ChatMessageEntity.java
│   ├── ChatMessageRepository.java
│   └── dto/
│       ├── SendMessageRequest.java
│       └── ChatMessageResponse.java
│
├── 📦 photo/                         🔜 RELATÓRIO FOTOGRÁFICO
│   ├── PhotoController.java
│   ├── PhotoExceptionHandler.java
│   ├── UploadPhotoUseCase.java
│   ├── PhotoReportEntity.java
│   ├── PhotoReportRepository.java
│   └── dto/
│       ├── UploadPhotoRequest.java
│       └── PhotoResponse.java
│
├── 📦 notification/                  🔜 NOTIFICAÇÕES
│   ├── NotificationController.java
│   ├── CreateNotificationUseCase.java
│   ├── MarkAsReadUseCase.java
│   ├── NotificationEntity.java
│   ├── NotificationRepository.java
│   └── dto/
│       ├── NotificationResponse.java
│       └── CreateNotificationRequest.java
│
└── 🔧 shared/                        ✅ JÁ IMPLEMENTADO
    ├── config/
    │   ├── SecurityConfig.java
    │   ├── TokenConfig.java
    │   ├── SecurityFilter.java
    │   └── AuthConfig.java
    └── exception/
        ├── ErrorResponse.java
        └── GlobalExceptionHandler.java
```

---

## 🗄️ Migrations (Flyway)

### Estrutura de Migrations

```
src/main/resources/db/migration/
├── V1__create_users_table.sql              ✅ JÁ EXISTE
├── V2__create_projects_table.sql           🔜 PRÓXIMO
├── V3__create_project_participants.sql
├── V4__create_tasks_table.sql
├── V5__create_task_dependencies.sql
├── V6__create_payment_milestones.sql
├── V7__create_financial_observations.sql
├── V8__create_daily_logs.sql
├── V9__create_daily_log_staff.sql
├── V10__create_chat_messages.sql
├── V11__create_photo_reports.sql
└── V12__create_notifications.sql
```

### ⚠️ IMPORTANTE: Ajustar User para UUID

**Problema Atual**: A tabela `users` já existe com `id` como VARCHAR(36).

**Opções:**

1. **Opção A - Recriar (Desenvolvimento)**
   ```bash
   # Dropar banco e recriar
   DROP DATABASE buildup;
   CREATE DATABASE buildup;
   ```

2. **Opção B - Migration de Ajuste (Produção)**
   ```sql
   -- V1_1__migrate_users_to_uuid.sql
   ALTER TABLE users 
   MODIFY COLUMN id BINARY(16) NOT NULL;
   ```

---

## 📝 Migrations Completas com UUID

### V2__create_projects_table.sql

```sql
CREATE TABLE projects (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    name VARCHAR(255) NOT NULL,
    client_name VARCHAR(255),
    description TEXT,
    start_date DATE,
    due_date DATE,
    contract_value DECIMAL(15, 2),
    down_payment DECIMAL(15, 2),
    cover_image_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_project_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'ON_HOLD', 'CANCELLED'))
);

CREATE INDEX idx_project_status ON projects(status);
CREATE INDEX idx_project_dates ON projects(start_date, due_date);
```

### V3__create_project_participants.sql

```sql
CREATE TABLE project_participants (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    user_id BINARY(16) NOT NULL,
    project_id BINARY(16) NOT NULL,
    role VARCHAR(100) NOT NULL,
    permission VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_permission CHECK (permission IN ('VIEW', 'EDIT', 'ADMIN')),
    UNIQUE KEY uk_user_project (user_id, project_id)
);

CREATE INDEX idx_participant_user ON project_participants(user_id);
CREATE INDEX idx_participant_project ON project_participants(project_id);
```

### V4__create_tasks_table.sql

```sql
CREATE TABLE tasks (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    project_id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    duration_days INT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    responsible_user_id BINARY(16),
    progress_percentage INT DEFAULT 0,
    order_index INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (responsible_user_id) REFERENCES users(id) ON DELETE SET NULL,
    
    CONSTRAINT chk_task_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_progress CHECK (progress_percentage BETWEEN 0 AND 100)
);

CREATE INDEX idx_task_project ON tasks(project_id);
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_responsible ON tasks(responsible_user_id);
CREATE INDEX idx_task_dates ON tasks(start_date, end_date);
```

### V5__create_task_dependencies.sql

```sql
CREATE TABLE task_dependencies (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    task_id BINARY(16) NOT NULL,
    depends_on_task_id BINARY(16) NOT NULL,
    dependency_type VARCHAR(50) DEFAULT 'FINISH_TO_START',
    
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (depends_on_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_no_self_dependency CHECK (task_id != depends_on_task_id),
    CONSTRAINT chk_dependency_type CHECK (dependency_type IN ('FINISH_TO_START', 'START_TO_START', 'FINISH_TO_FINISH', 'START_TO_FINISH')),
    UNIQUE KEY uk_task_dependency (task_id, depends_on_task_id)
);

CREATE INDEX idx_dependency_task ON task_dependencies(task_id);
CREATE INDEX idx_dependency_depends_on ON task_dependencies(depends_on_task_id);
```

### V6__create_payment_milestones.sql

```sql
CREATE TABLE payment_milestones (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    project_id BINARY(16) NOT NULL,
    milestone_number INT NOT NULL,
    value DECIMAL(15, 2) NOT NULL,
    invoice_number VARCHAR(100),
    due_date DATE NOT NULL,
    payment_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'PAID', 'LATE', 'CANCELLED')),
    CONSTRAINT chk_milestone_value CHECK (value > 0),
    UNIQUE KEY uk_project_milestone (project_id, milestone_number)
);

CREATE INDEX idx_milestone_project ON payment_milestones(project_id);
CREATE INDEX idx_milestone_status ON payment_milestones(status);
CREATE INDEX idx_milestone_due_date ON payment_milestones(due_date);
```

### V7__create_financial_observations.sql

```sql
CREATE TABLE financial_observations (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    project_id BINARY(16) NOT NULL,
    observation TEXT NOT NULL,
    created_by BINARY(16),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_financial_obs_project ON financial_observations(project_id);
CREATE INDEX idx_financial_obs_created_at ON financial_observations(created_at);
```

### V8__create_daily_logs.sql

```sql
CREATE TABLE daily_logs (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    project_id BINARY(16) NOT NULL,
    log_date DATE NOT NULL,
    weather_morning VARCHAR(50),
    weather_afternoon VARCHAR(50),
    weather_night VARCHAR(50),
    ongoing_activities TEXT,
    client_comments TEXT,
    technical_responsible_signature VARCHAR(500),
    client_signature VARCHAR(500),
    created_by BINARY(16),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    
    UNIQUE KEY uk_project_date (project_id, log_date)
);

CREATE INDEX idx_daily_log_project ON daily_logs(project_id);
CREATE INDEX idx_daily_log_date ON daily_logs(log_date);
```

### V9__create_daily_log_staff.sql

```sql
CREATE TABLE daily_log_staff (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    daily_log_id BINARY(16) NOT NULL,
    role VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    
    FOREIGN KEY (daily_log_id) REFERENCES daily_logs(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_staff_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_staff_daily_log ON daily_log_staff(daily_log_id);
```

### V10__create_chat_messages.sql

```sql
CREATE TABLE chat_messages (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    project_id BINARY(16) NOT NULL,
    sender_user_id BINARY(16) NOT NULL,
    message_content TEXT,
    attachment_url VARCHAR(500),
    attachment_type VARCHAR(50),
    attachment_name VARCHAR(255),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_project ON chat_messages(project_id);
CREATE INDEX idx_chat_sender ON chat_messages(sender_user_id);
CREATE INDEX idx_chat_timestamp ON chat_messages(timestamp);
CREATE INDEX idx_chat_is_read ON chat_messages(is_read);
```

### V11__create_photo_reports.sql

```sql
CREATE TABLE photo_reports (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    project_id BINARY(16) NOT NULL,
    title VARCHAR(255),
    photo_url VARCHAR(500) NOT NULL,
    description TEXT,
    taken_date DATE,
    uploaded_by BINARY(16),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_photo_project ON photo_reports(project_id);
CREATE INDEX idx_photo_date ON photo_reports(taken_date);
CREATE INDEX idx_photo_uploaded_by ON photo_reports(uploaded_by);
```

### V12__create_notifications.sql

```sql
CREATE TABLE notifications (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    user_id BINARY(16) NOT NULL,
    project_id BINARY(16),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_notification_type CHECK (type IN ('PROJECT_UPDATE', 'TASK_ASSIGNED', 'PAYMENT_DUE', 'CHAT_MESSAGE', 'DAILY_LOG_CREATED', 'GENERAL'))
);

CREATE INDEX idx_notification_user ON notifications(user_id);
CREATE INDEX idx_notification_project ON notifications(project_id);
CREATE INDEX idx_notification_is_read ON notifications(is_read);
CREATE INDEX idx_notification_created_at ON notifications(created_at);
```

---

## 🚀 Ordem de Implementação

### Fase 1: Fundação (✅ COMPLETO)
- [x] Auth (Login/Register)
- [x] Testcontainers
- [x] Package-by-Feature
- [x] Exception Handling

### Fase 2: Gestão de Projetos (🔜 PRÓXIMO)

#### 2.1 Criar Migrations
```bash
# Criar arquivos de migration
touch src/main/resources/db/migration/V2__create_projects_table.sql
touch src/main/resources/db/migration/V3__create_project_participants.sql
```

#### 2.2 Implementar Entities

**ProjectEntity.java** (seguindo padrão já estabelecido)
```java
package com.maistech.buildup.project;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
public class ProjectEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    @Setter
    private String name;

    @Column(name = "client_name")
    @Setter
    private String clientName;

    @Column(columnDefinition = "TEXT")
    @Setter
    private String description;

    @Column(name = "start_date")
    @Setter
    private LocalDate startDate;

    @Column(name = "due_date")
    @Setter
    private LocalDate dueDate;

    @Column(name = "contract_value", precision = 15, scale = 2)
    @Setter
    private BigDecimal contractValue;

    @Column(name = "down_payment", precision = 15, scale = 2)
    @Setter
    private BigDecimal downPayment;

    @Column(name = "cover_image_url", length = 500)
    @Setter
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    @Setter
    private ProjectStatus status = ProjectStatus.IN_PROGRESS;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ProjectStatus {
        IN_PROGRESS,
        COMPLETED,
        ON_HOLD,
        CANCELLED
    }
}
```

#### 2.3 Implementar Repository

**ProjectRepository.java**
```java
package com.maistech.buildup.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    @Query("""
        SELECT p FROM ProjectEntity p 
        JOIN ProjectParticipantEntity pp ON pp.projectId = p.id 
        WHERE pp.userId = :userId
        """)
    List<ProjectEntity> findProjectsByUserId(@Param("userId") UUID userId);

    List<ProjectEntity> findByStatus(ProjectEntity.ProjectStatus status);
}
```

#### 2.4 Implementar DTOs

**CreateProjectRequest.java**
```java
package com.maistech.buildup.project.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateProjectRequest(
    @NotEmpty(message = "Project name is required")
    String name,
    
    String clientName,
    String description,
    
    @NotNull(message = "Start date is required")
    LocalDate startDate,
    
    LocalDate dueDate,
    
    @Positive(message = "Contract value must be positive")
    BigDecimal contractValue,
    
    BigDecimal downPayment,
    String coverImageUrl,
    
    List<ParticipantInput> participants
) {}
```

**ParticipantInput.java**
```java
package com.maistech.buildup.project.dto;

import com.maistech.buildup.project.ProjectParticipantEntity.Permission;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ParticipantInput(
    @NotNull UUID userId,
    @NotNull String role,
    @NotNull Permission permission
) {}
```

**ProjectResponse.java**
```java
package com.maistech.buildup.project.dto;

import com.maistech.buildup.project.ProjectEntity.ProjectStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(
    UUID id,
    String name,
    String clientName,
    String description,
    LocalDate startDate,
    LocalDate dueDate,
    BigDecimal contractValue,
    BigDecimal downPayment,
    String coverImageUrl,
    ProjectStatus status
) {}
```

#### 2.5 Implementar UseCase

**CreateProjectUseCase.java**
```java
package com.maistech.buildup.project;

import com.maistech.buildup.project.dto.CreateProjectRequest;
import com.maistech.buildup.project.dto.ProjectResponse;
import com.maistech.buildup.project.dto.ParticipantInput;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Component
public class CreateProjectUseCase {

    private final ProjectRepository projectRepository;
    private final ProjectParticipantRepository participantRepository;

    public CreateProjectUseCase(
        ProjectRepository projectRepository,
        ProjectParticipantRepository participantRepository
    ) {
        this.projectRepository = projectRepository;
        this.participantRepository = participantRepository;
    }

    @Transactional
    public ProjectResponse execute(CreateProjectRequest request, UUID currentUserId) {
        // Create project
        ProjectEntity project = new ProjectEntity();
        project.setName(request.name());
        project.setClientName(request.clientName());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setDueDate(request.dueDate());
        project.setContractValue(request.contractValue());
        project.setDownPayment(request.downPayment());
        project.setCoverImageUrl(request.coverImageUrl());
        
        project = projectRepository.save(project);
        
        // Add creator as ADMIN
        addParticipant(project.getId(), currentUserId, "Project Manager", 
                      ProjectParticipantEntity.Permission.ADMIN);
        
        // Add other participants
        if (request.participants() != null) {
            for (ParticipantInput p : request.participants()) {
                addParticipant(project.getId(), p.userId(), p.role(), p.permission());
            }
        }
        
        return mapToResponse(project);
    }

    private void addParticipant(UUID projectId, UUID userId, String role, 
                               ProjectParticipantEntity.Permission permission) {
        ProjectParticipantEntity participant = new ProjectParticipantEntity();
        participant.setProjectId(projectId);
        participant.setUserId(userId);
        participant.setRole(role);
        participant.setPermission(permission);
        participantRepository.save(participant);
    }

    private ProjectResponse mapToResponse(ProjectEntity project) {
        return new ProjectResponse(
            project.getId(),
            project.getName(),
            project.getClientName(),
            project.getDescription(),
            project.getStartDate(),
            project.getDueDate(),
            project.getContractValue(),
            project.getDownPayment(),
            project.getCoverImageUrl(),
            project.getStatus()
        );
    }
}
```

#### 2.6 Implementar Controller

**ProjectController.java**
```java
package com.maistech.buildup.project;

import com.maistech.buildup.project.dto.CreateProjectRequest;
import com.maistech.buildup.project.dto.ProjectResponse;
import com.maistech.buildup.shared.config.JTWUserData;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final CreateProjectUseCase createProjectUseCase;

    public ProjectController(CreateProjectUseCase createProjectUseCase) {
        this.createProjectUseCase = createProjectUseCase;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
        @Valid @RequestBody CreateProjectRequest request,
        Authentication authentication
    ) {
        JTWUserData userData = (JTWUserData) authentication.getPrincipal();
        ProjectResponse response = createProjectUseCase.execute(request, userData.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

#### 2.7 Implementar Exception Handler

**ProjectExceptionHandler.java**
```java
package com.maistech.buildup.project;

import com.maistech.buildup.shared.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
public class ProjectExceptionHandler {

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProjectNotFound(ProjectNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InsufficientPermissionException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPermission(InsufficientPermissionException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}
```

#### 2.8 Implementar Testes

**CreateProjectIntegrationTest.java**
```java
package com.maistech.buildup.project;

import com.maistech.buildup.project.dto.CreateProjectRequest;
import com.maistech.buildup.project.dto.ProjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CreateProjectIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateProjectSuccessfully() {
        // First, login to get token
        // ... authentication logic
        
        CreateProjectRequest request = new CreateProjectRequest(
            "Shopping Center Construction",
            "ABC Corp",
            "New shopping center project",
            LocalDate.now(),
            LocalDate.now().plusMonths(12),
            new BigDecimal("1000000.00"),
            new BigDecimal("100000.00"),
            null,
            null
        );

        ResponseEntity<ProjectResponse> response = restTemplate.postForEntity(
            "/projects",
            request,
            ProjectResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Shopping Center Construction");
    }
}
```

---

## ✅ Checklist de Qualidade por Feature

Para cada feature implementada, verificar:

### 🏗️ Estrutura
- [ ] Feature em package próprio (`project/`, `task/`, etc.)
- [ ] Entities com `@Getter` e setters seletivos
- [ ] Repository interface simples
- [ ] UseCases com lógica de negócio
- [ ] Controller apenas delegando
- [ ] DTOs para request/response
- [ ] Exception handler próprio da feature

### 🔒 Segurança
- [ ] Validação com `@Valid` nos controllers
- [ ] Verificação de permissões nos UseCases
- [ ] Campos sensíveis não expostos

### 🧪 Testes
- [ ] Integration test com Testcontainers
- [ ] Unit test dos UseCases
- [ ] Casos de sucesso e falha
- [ ] Todos os testes passando

### 📝 Código Limpo
- [ ] Constructor injection
- [ ] Sem `@Autowired` em fields
- [ ] Sem comentários excessivos
- [ ] Naming conventions consistente
- [ ] Métodos curtos e focados

### 🗄️ Database
- [ ] Migration com UUID
- [ ] Índices apropriados
- [ ] Foreign keys corretas
- [ ] Constraints validando dados

---

## 📚 Recursos e Referências

- [Tomato Architecture](https://tomato-architecture.github.io/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Testcontainers](https://testcontainers.com/)
- [Flyway](https://flywaydb.org/)
- [AssertJ](https://assertj.github.io/doc/)

---

## 🎯 Próximos Passos

1. **Executar migrations** para criar todas as tabelas
2. **Implementar feature Project** seguindo o exemplo acima
3. **Testar com Postman/Insomnia**
4. **Adicionar feature Task** (cronograma)
5. **Continuar com as demais features**

---

## 💡 Dicas Importantes

### UUID no MySQL
```sql
-- Converter UUID para BINARY(16) ao inserir
INSERT INTO projects (id, name) 
VALUES (UUID_TO_BIN(UUID()), 'Project Name');

-- Converter BINARY(16) para UUID ao consultar
SELECT BIN_TO_UUID(id) as id, name 
FROM projects;
```

### JPA com UUID e BINARY(16)
```java
@Id
@GeneratedValue(generator = "UUID")
@GenericGenerator(
    name = "UUID",
    strategy = "org.hibernate.id.UUIDGenerator"
)
@Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
private UUID id;
```

### Hibernate Dependency
```xml
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
</dependency>
```

---

**Bom desenvolvimento! 🚀**
