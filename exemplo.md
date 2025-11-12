# 🍅 Guia de Migração para Tomato Architecture

## 📋 Status Atual

### ✅ O que já está conforme:
- Package by feature (`auth/`, `project/`)
- Constructor injection
- No field injection (`@Autowired`)
- Testcontainers
- `@ConfigurationProperties` para config
- Exception handlers por feature

### 🔄 O que precisa migrar:
- **UseCases separados** → consolidar em **Services**
- **Entities anêmicas** → adicionar **domain logic**
- **Repository vazando** → encapsular Spring Data (futuro)

---

## 🚀 Passo 1: Refatorar Auth Feature

### 1.1 Consolidar UseCases em AuthService

**Arquivos a modificar:**
- ✅ Criar: `AuthService.java`
- 🗑️ Deletar: `LoginUseCase.java`
- 🗑️ Deletar: `RegisterUserUseCase.java`
- 🔄 Atualizar: `AuthController.java`
- 🔄 Atualizar: Todos os testes

**Passo a passo:**

```bash
# 1. Criar AuthService.java
```

```java
package com.maistech.buildup.auth;

import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.dto.LoginResponse;
import com.maistech.buildup.auth.dto.RegisterUserRequest;
import com.maistech.buildup.auth.dto.RegisterUserResponse;
import com.maistech.buildup.auth.exception.InvalidPasswordException;
import com.maistech.buildup.auth.exception.UserAlreadyExistsException;
import com.maistech.buildup.shared.config.TokenConfig;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenConfig tokenConfig;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            TokenConfig tokenConfig
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenConfig = tokenConfig;
    }

    public LoginResponse login(LoginRequest request) {
        var credentials = new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
        );

        var authentication = authenticationManager.authenticate(credentials);
        var user = (UserEntity) authentication.getPrincipal();
        var token = tokenConfig.generateToken(user);

        return new LoginResponse(token, user.getName(), user.getEmail());
    }

    public RegisterUserResponse register(RegisterUserRequest request) {
        validateUserDoesNotExist(request.email());
        validatePasswordStrength(request.password());

        var user = createUserFromRequest(request);
        var savedUser = userRepository.save(user);

        return new RegisterUserResponse(
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    private void validateUserDoesNotExist(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(
                    "User with email " + email + " already exists"
            );
        }
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new InvalidPasswordException(
                    "Password must be at least 8 characters"
            );
        }
    }

    private UserEntity createUserFromRequest(RegisterUserRequest request) {
        var user = new UserEntity();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        return user;
    }
}
```

```bash
# 2. Atualizar AuthController.java
```

```java
package com.maistech.buildup.auth;

import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.dto.LoginResponse;
import com.maistech.buildup.auth.dto.RegisterUserRequest;
import com.maistech.buildup.auth.dto.RegisterUserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(
        @Valid @RequestBody RegisterUserRequest request
    ) {
        RegisterUserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

```bash
# 3. Criar InvalidPasswordException.java
```

```java
package com.maistech.buildup.auth;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
```

```bash
# 4. Deletar UseCases antigos
rm src/main/java/com/maistech/buildup/auth/LoginUseCase.java
rm src/main/java/com/maistech/buildup/auth/RegisterUserUseCase.java
```

---

### 1.2 Adicionar Domain Logic em UserEntity

```java
package com.maistech.buildup.auth;

import com.maistech.buildup.auth.exception.InvalidPasswordException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Table(name = "users")
@Getter
public class UserEntity implements UserDetails {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$"
    );
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Setter
    private String name;

    @Email
    @Setter
    private String email;

    @Setter
    private String password;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ============ Domain Logic ============

    public boolean hasValidEmail() {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isPasswordStrong(String rawPassword) {
        return rawPassword != null && rawPassword.length() >= MIN_PASSWORD_LENGTH;
    }

    public void changePassword(String newPassword, PasswordEncoder encoder) {
        if (!isPasswordStrong(newPassword)) {
            throw new InvalidPasswordException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters"
            );
        }
        this.password = encoder.encode(newPassword);
    }

    public boolean isRecentlyCreated() {
        return createdAt != null &&
                createdAt.isAfter(LocalDateTime.now().minusDays(7));
    }

    public void updateProfile(String newName, String newEmail) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (newEmail == null || !EMAIL_PATTERN.matcher(newEmail).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.name = newName;
        this.email = newEmail;
    }

    // ============ UserDetails Implementation ============

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

---

### 1.3 Atualizar Testes

**LoginIntegrationTest.java** (atualizado):

```java
package com.maistech.buildup.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.dto.LoginResponse;
import com.maistech.buildup.auth.dto.RegisterUserRequest;
import org.junit.jupiter.api.AfterEach;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class LoginIntegrationTest {

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

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        RegisterUserRequest registerRequest = new RegisterUserRequest(
            "John Doe",
            "john@example.com",
            "password123"
        );
        restTemplate.postForEntity("/auth/register", registerRequest, Object.class);

        LoginRequest loginRequest = new LoginRequest("john@example.com", "password123");
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
            "/auth/login",
            loginRequest,
            LoginResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotBlank();
        assertThat(response.getBody().name()).isEqualTo("John Doe");
        assertThat(response.getBody().email()).isEqualTo("john@example.com");
    }
}
```

**AuthServiceTest.java** (novo - substitui LoginUseCaseTest e RegisterUserUseCaseTest):

```java
package com.maistech.buildup.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.dto.RegisterUserRequest;
import com.maistech.buildup.auth.exception.InvalidPasswordException;
import com.maistech.buildup.auth.exception.UserAlreadyExistsException;
import com.maistech.buildup.shared.config.TokenConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenConfig tokenConfig;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        LoginRequest request = new LoginRequest("john@example.com", "password123");

        UserEntity user = new UserEntity();
        user.setEmail("john@example.com");
        user.setName("John Doe");

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenConfig.generateToken(user)).thenReturn("mock-jwt-token");

        var response = authService.login(request);

        assertThat(response.token()).isEqualTo("mock-jwt-token");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.name()).isEqualTo("John Doe");

        verify(authenticationManager).authenticate(any());
        verify(tokenConfig).generateToken(user);
    }

    @Test
    void shouldThrowExceptionWhenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager).authenticate(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterUserRequest request = new RegisterUserRequest(
                "John Doe",
                "john@example.com",
                "password123"
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("john@example.com");

        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRegisterUserWhenEmailDoesNotExist() {
        RegisterUserRequest request = new RegisterUserRequest(
                "John Doe",
                "john@example.com",
                "password123"
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        UserEntity savedUser = new UserEntity();
        savedUser.setName("John Doe");
        savedUser.setEmail("john@example.com");
        savedUser.setPassword("encodedPassword");

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        var response = authService.register(request);

        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.email()).isEqualTo("john@example.com");

        verify(userRepository).existsByEmail("john@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsTooShort() {
        RegisterUserRequest request = new RegisterUserRequest(
                "John Doe",
                "john@example.com",
                "short"
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("at least 8 characters");

        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any());
    }
}
```

```bash
# Deletar testes antigos
rm src/test/java/com/maistech/buildup/auth/LoginUseCaseTest.java
rm src/test/java/com/maistech/buildup/auth/RegisterUserUseCaseTest.java
```

---

## 🚀 Passo 2: Implementar Project Feature (já seguindo Tomato)

Ao implementar novas features, **já siga o padrão Tomato desde o início**:

### 2.1 Estrutura de Arquivos

```
project/
├── ProjectController.java         ✅ Apenas delega
├── ProjectService.java             ✅ Business logic (NÃO UseCases)
├── ProjectExceptionHandler.java    ✅
├── ProjectEntity.java              ✅ Com domain logic
├── ProjectParticipantEntity.java   ✅ Com domain logic
├── ProjectRepository.java          ✅
├── ProjectParticipantRepository.java ✅
├── ProjectNotFoundException.java
├── InsufficientPermissionException.java
└── dto/
    ├── CreateProjectRequest.java
    ├── UpdateProjectRequest.java
    ├── ProjectResponse.java
    └── ParticipantInput.java
```

### 2.2 ProjectService.java (exemplo completo)

```java
package com.maistech.buildup.project;

import com.maistech.buildup.project.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.List;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectParticipantRepository participantRepository;

    public ProjectService(
        ProjectRepository projectRepository,
        ProjectParticipantRepository participantRepository
    ) {
        this.projectRepository = projectRepository;
        this.participantRepository = participantRepository;
    }

    public ProjectResponse createProject(CreateProjectRequest request, UUID currentUserId) {
        ProjectEntity project = new ProjectEntity();
        project.setName(request.name());
        project.setClientName(request.clientName());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setDueDate(request.dueDate());
        project.setContractValue(request.contractValue());
        project.setDownPayment(request.downPayment());
        project.setCoverImageUrl(request.coverImageUrl());
        
        ProjectEntity saved = projectRepository.save(project);
        
        addParticipant(saved.getId(), currentUserId, "Project Manager", 
                      ProjectParticipantEntity.Permission.ADMIN);
        
        if (request.participants() != null) {
            for (ParticipantInput p : request.participants()) {
                addParticipant(saved.getId(), p.userId(), p.role(), p.permission());
            }
        }
        
        return mapToResponse(saved);
    }

    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request, UUID currentUserId) {
        ProjectEntity project = findProjectOrThrow(projectId);
        validateUserHasPermission(projectId, currentUserId, ProjectParticipantEntity.Permission.EDIT);
        
        project.updateDetails(
            request.name(),
            request.clientName(),
            request.description(),
            request.startDate(),
            request.dueDate()
        );
        
        ProjectEntity updated = projectRepository.save(project);
        return mapToResponse(updated);
    }

    public void deleteProject(UUID projectId, UUID currentUserId) {
        validateUserHasPermission(projectId, currentUserId, ProjectParticipantEntity.Permission.ADMIN);
        projectRepository.deleteById(projectId);
    }

    public ProjectResponse getProjectDetails(UUID projectId, UUID currentUserId) {
        ProjectEntity project = findProjectOrThrow(projectId);
        validateUserHasPermission(projectId, currentUserId, ProjectParticipantEntity.Permission.VIEW);
        return mapToResponse(project);
    }

    public List<ProjectResponse> getUserProjects(UUID userId) {
        return projectRepository.findProjectsByUserId(userId)
            .stream()
            .map(this::mapToResponse)
            .toList();
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

    private ProjectEntity findProjectOrThrow(UUID projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + projectId));
    }

    private void validateUserHasPermission(UUID projectId, UUID userId, 
                                          ProjectParticipantEntity.Permission requiredPermission) {
        participantRepository.findByProjectIdAndUserId(projectId, userId)
            .ifPresentOrElse(
                participant -> {
                    if (!participant.hasPermission(requiredPermission)) {
                        throw new InsufficientPermissionException(
                            "User does not have " + requiredPermission + " permission"
                        );
                    }
                },
                () -> {
                    throw new InsufficientPermissionException(
                        "User is not a participant of this project"
                    );
                }
            );
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

### 2.3 ProjectEntity.java (com domain logic)

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
    @GeneratedValue(strategy = GenerationType.UUID)
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

    // ============ Domain Logic ============

    public boolean isOverdue() {
        return dueDate != null && 
               LocalDate.now().isAfter(dueDate) && 
               status != ProjectStatus.COMPLETED;
    }

    public boolean canBeCompleted() {
        return status == ProjectStatus.IN_PROGRESS;
    }

    public void complete() {
        if (!canBeCompleted()) {
            throw new IllegalStateException("Project cannot be completed in status: " + status);
        }
        this.status = ProjectStatus.COMPLETED;
    }

    public boolean canBeDeleted() {
        return status != ProjectStatus.COMPLETED;
    }

    public BigDecimal getRemainingPayment() {
        if (contractValue == null) return BigDecimal.ZERO;
        if (downPayment == null) return contractValue;
        return contractValue.subtract(downPayment);
    }

    public int getDaysUntilDueDate() {
        if (dueDate == null) return -1;
        return (int) LocalDate.now().until(dueDate).getDays();
    }

    public void updateDetails(String name, String clientName, String description, 
                             LocalDate startDate, LocalDate dueDate) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (clientName != null) {
            this.clientName = clientName;
        }
        if (description != null) {
            this.description = description;
        }
        if (startDate != null) {
            this.startDate = startDate;
        }
        if (dueDate != null) {
            validateDueDate(startDate, dueDate);
            this.dueDate = dueDate;
        }
    }

    private void validateDueDate(LocalDate start, LocalDate due) {
        if (start != null && due.isBefore(start)) {
            throw new IllegalArgumentException("Due date cannot be before start date");
        }
    }

    public enum ProjectStatus {
        IN_PROGRESS,
        COMPLETED,
        ON_HOLD,
        CANCELLED
    }
}
```

---

## 📝 Resumo das Mudanças

### ✅ Antes (UseCases):
```
auth/
├── LoginUseCase.java
├── RegisterUserUseCase.java
└── AuthController.java
```

### ✅ Depois (Service):
```
auth/
├── AuthService.java              ← Consolidado
├── AuthController.java           ← Injeta AuthService
└── UserEntity.java                ← Com domain logic
```

---

## 🧪 Executar Testes

```bash
# Rodar todos os testes
./mvnw test

# Rodar apenas integration tests
./mvnw test -Dtest="*IntegrationTest"

# Rodar apenas unit tests
./mvnw test -Dtest="*Test" -Dtest="!*IntegrationTest"
```

---

## ✅ Checklist Final

Após migração, verificar:

- [ ] Todos os UseCases foram consolidados em Services
- [ ] Services anotados com `@Service` e `@Transactional`
- [ ] Entities têm domain logic (não anêmicas)
- [ ] Controllers apenas delegam
- [ ] Constructor injection em todos os componentes
- [ ] Todos os testes passando
- [ ] Integration tests com Testcontainers
- [ ] Unit tests apenas para lógica complexa
- [ ] Sem interfaces desnecessárias

---

## 🎯 Próximos Passos

1. **Implementar Project feature** seguindo o padrão Tomato
2. **Adicionar domain logic** em ProjectEntity
3. **Criar integration tests** com Testcontainers
4. **Continuar com Task feature**

---

**Boa refatoração! 🍅**