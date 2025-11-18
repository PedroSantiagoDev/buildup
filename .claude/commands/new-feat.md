---
description: Plan and implement a new feature following Java/Spring guidelines with focus on clarity, observability (Actuator), security (Spring Security), and testing (Testcontainers).
argument-hint: [feature description]
---

# New Feature Development (Java/Spring)

Plan and implement a new feature: $ARGUMENTS

## Approach:
1. **Analysis & Planning**
    - Analyze existing architecture (`Controller`, `Service`, `Repository` layers).
    - Identify dependencies and integration points.

2. **Design (Interface First)**
    - Define the `Service` interface.
    - Define input and output DTOs (Data Transfer Objects).
    - Define Controller endpoints (e.g., `@PostMapping`).

3. **Implementation Guidelines**
    - **Clarity:** Descriptive names (no abbreviations).
    - **Java:** Use Immutability (`final` fields), `try-with-resources`, correct `Optional` usage, no Raw Types.
    - **Spring:** **Constructor**-based dependency injection. Use `@ConfigurationProperties` for configuration.
    - **Layers:** Keep business logic in `Services`. `Controllers` should be thin.

4. **Core Implementation Focus**
    - **Robustness:** Validate inputs (e.g., `javax.validation` or `jakarta.validation` on DTOs).
    - **Observability:** Ensure `Actuator` is active. Add clear logs.
    - **Security:** Apply Spring Security to new endpoints.
    - **Data Access:** Use Spring Data JPA (Repositories).

5. **Testing Strategy**
    - **Unit:** Test `Service` logic (mocking the `Repository`).
    - **Integration (Slice):** Test the `Controller` (`@WebMvcTest`) and `Repository` (`@DataJpaTest`).
    - **Integration (Full):** Use `@SpringBootTest` and **Testcontainers** to validate the E2E flow against a real database.

6. **Code Quality**
    - Avoid unnecessary comments (code should be readable).
    - Use Method References and Streams where appropriate.
    - Refer to objects by their interfaces (`List` not `ArrayList`).

7. **Final Checks**
    - Run `mvn clean verify` (or Gradle).
    - Manually validate endpoints (if needed).
    - ensure no sensitive data is logged.

Focus on clear, robust, secure, observable, and testable code that follows established Spring Boot patterns.