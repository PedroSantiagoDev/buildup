---
description: Comprehensive code review focusing on clarity, observability (Actuator), security (Spring Security), and Java/Spring patterns (DI, Testcontainers).
allowed-tools: Bash(git:*)
---

# Code Review Analysis (Java/Spring Boot)

Review current changes for clarity, observability, security, and performance per Java/Spring guidelines.

## Review Process:
1. **Change Overview**
    - Analyze `git diff` to understand scope.
    - Review commit history.

2. **Clarity & Java Standards Review**
    - **Naming:** Descriptive names? (no abbreviations, `data`, `item`).
    - **Immutability:** Are DI fields and properties `final`?
    - **Types:** No Raw Types? Correct `Optional` usage?
    - **Resources:** Is `try-with-resources` used?
    - **Contracts:** `equals/hashCode` implemented correctly?
    - **Complexity:** Are methods short and have a single responsibility?

3. **Spring Patterns**
    - **DI:** Is **constructor injection** used? (❌ Field injection).
    - **Configuration:** `@ConfigurationProperties` (preferred) or `@Value`?
    - **Layers:** Is logic separated correctly (`Controller`, `Service`, `Repository`)?
    - **`WebClient`:** Is `WebClient` used instead of `RestTemplate`?
    - **Transactions:** Is `@Transactional` applied correctly (on the `Service` layer)?

4. **Observability Review**
    - **`Actuator`:** Are endpoints (`/health`, `/metrics`) exposed and configured?
    - **Logging:** Structured logs with no sensitive data?

5. **Security Review (Spring Security)**
    - **Endpoints:** Are new endpoints secured?
    - **Validation:** Are inputs validated? (e.g., `@Valid` on DTOs).

6. **Testing Review**
    - **Coverage:** Do tests exist for the new logic?
    - **Test Type:** Unit (Mockito) and Integration (`@SpringBootTest`) tests?
    - **`Testcontainers`:** Is it used for database integration tests?
    - **Assertions:** Testing behavior, not implementation?

## Output Format:
### Summary
- [Brief overview of changes and scope]

### Critical Issues (Must Fix)
- [Security vulnerabilities (e.g., unsecured endpoint)]
- [Java contract violations (e.g., equals/hashCode)]
- [Use of field-based dependency injection (`@Autowired` on field)]
- [Use of Raw Types]

### Major Issues (Should Fix)
- [Business logic in the Controller]
- [Missing tests for new feature]
- [Use of `RestTemplate`]

### Minor Issues (Consider)
- [Naming could be clearer]
- [Opportunity to use Method Reference]

### Recommendations
- [Specific pattern suggestions]

### Approval Status
- ✅ Approved | ⚠️ Approved with minor changes | ❌ Requires changes