# Tomato Architecture Guidelines

## Core Principles
- Simplicity over excessive abstraction
- Package by feature (not by layer)
- Business logic in Application Core (Service layer)
- Test entire features, not just units
- Embrace framework capabilities (Spring Boot)
- No unnecessary interfaces

## Architecture Rules

### 1. Separation of Concerns
**Controllers (Web Layer)**
- Extract data from requests
- Validate input (Bean Validation)
- Delegate to Service layer
- Never contain business logic
- Never access repositories directly

**Services (Application Core)**
- Contain all business logic
- Execute use cases as atomic operations (@Transactional)
- Independent of delivery mechanism (web, CLI, jobs)
- Can be invoked from any context

**Repositories (Persistence Layer)**
- Only responsible for database access
- Hide persistence framework details from Service
- Use Spring Data JPA interfaces

### 2. Package Structure
Organize by feature, not layer:

```
com.maistech.buildup
├── auth
│   ├── AuthController
│   ├── AuthService
│   ├── dto (LoginRequest, LoginResponse, etc)
│   └── UserRepository
├── customer
│   ├── CustomerController
│   ├── CustomerService
│   ├── dto
│   └── CustomerRepository
└── config (security, JWT, etc)
```

### 3. Dependency Injection
- Always use constructor injection
- Declare fields as `final`
- No `@Autowired` on fields

### 4. Domain Logic
Keep domain behavior inside domain objects:
```java
// ❌ DON'T
class OrderService {
    BigDecimal calculateTotal(Order order) { ... }
}

// ✅ DO
class Order {
    BigDecimal getTotal() { ... }
}
```

### 5. No Unnecessary Interfaces
- Only create interfaces when needed (multiple implementations)
- Mockito can mock concrete classes
- Don't create interfaces "just in case"

### 6. Testing Strategy
**Integration Tests (Primary)**
- Test complete features end-to-end
- Use Testcontainers for real dependencies
- More confidence than unit tests

**Unit Tests (Secondary)**
- Test complex business logic in isolation
- Use Mockito to mock dependencies
- Keep tests simple and meaningful

**Slice Tests**
- Use @WebMvcTest for controller validation tests
- Use @DataJpaTest for repository tests
- No need to spin up all dependencies

### 7. Configuration
- Use @ConfigurationProperties for type-safe config
- Never hardcode secrets
- Externalize configuration

### 8. Comments
- Comments are unnecessary 98% of the time
- Code should be self-documenting
- Extract to functions/variables instead
- Only explain "why", never "what"

## Anti-Patterns to Avoid

### ❌ Business Logic in Controllers
```java
@PostMapping("/customers")
void create(@RequestBody Customer customer) {
    if(service.existsByEmail(customer.getEmail())) {
        throw new EmailAlreadyInUseException();
    }
    customer.setCreatedAt(Instant.now());
    service.save(customer);
}
```

### ✅ Business Logic in Service
```java
@PostMapping("/customers")
void create(@RequestBody Customer customer) {
    service.save(customer);
}

@Service
@Transactional
class CustomerService {
    void save(Customer customer) {
        if(repository.existsByEmail(customer.getEmail())) {
            throw new EmailAlreadyInUseException();
        }
        customer.setCreatedAt(Instant.now());
        repository.save(customer);
    }
}
```

### ❌ Persistence Framework Leaking to Service
```java
@Service
class CustomerService {
    PagedResult<Customer> getCustomers(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo, 20, Sort.by("name"));
        Page<Customer> page = repository.findAll(pageable);
        return convert(page);
    }
}
```

### ✅ Encapsulate in Repository
```java
@Service
class CustomerService {
    PagedResult<Customer> getCustomers(Integer pageNo) {
        return repository.findAll(pageNo);
    }
}

@Repository
class CustomerRepository {
    PagedResult<Customer> findAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo, 20, Sort.by("name"));
        // handle pagination internally
    }
}
```

## Java Best Practices

### Immutability
- Prefer `final` fields
- Avoid Lombok `@Data` (generates setters)
- Use `@Value` for immutable DTOs or records
- For entities, use `@Getter` + manual setters only when needed

### Records for DTOs
```java
// ✅ Immutable by default
public record LoginRequest(
    @NotEmpty String email,
    @NotEmpty String password
) {}
```

### Entities
```java
// ✅ Use @Getter, avoid @Data
@Entity
@Table(name = "users")
@Getter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String email;

    // Domain logic belongs here
    public boolean hasValidEmail() {
        return email != null && email.contains("@");
    }
}
```

## Testing Examples

### Integration Test (Primary)
```java
@SpringBootTest
@Testcontainers
class CustomerFeatureTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private CustomerService service;

    @Test
    void shouldCreateCustomerSuccessfully() {
        var request = new CreateCustomerRequest("John", "john@example.com");

        var response = service.createCustomer(request);

        assertThat(response.email()).isEqualTo("john@example.com");
    }
}
```

### Unit Test (Secondary)
```java
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private CustomerService service;

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        when(repository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class,
            () -> service.createCustomer(new Customer(...)));
    }
}
```

## Commit Message Format
```
<verb> <what> [<context>]

Examples:
✅ Add customer creation feature
✅ Fix user validation on registration
✅ Refactor authentication to use JWT
❌ Updates
❌ Fixed stuff
```
