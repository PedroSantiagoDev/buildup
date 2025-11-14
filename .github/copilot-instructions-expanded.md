## Git Workflow
- Do not include Claude Code in commit messages
- Use conventional commits (be brief and descriptive)

# Software Engineering Concepts
- type-safety (aim for e2e type-safety with DTOs and Entities)
- monitoring/observability/profiling/tracing (via Spring Boot Actuator)
- simplicity (KISS, YAGNI, 1 idea per sentence)
- descriptive naming (avoid vague terms: data, item, list, component, info)
- functional programming (immutability, high order functions, streams)
- automated testing (JUnit, Mockito, Testcontainers)
- error-handling (always provide user feedback and log errors)
- writing (1 idea per sentence, lead with result, use active voice)
- focus on:
    - Robustness and Type Safety
    - Observability (Actuator, Micrometer)
    - Accessibility (a11y)
    - Security (Spring Security, OWASP)
- comments are unnecessary 98% of the time, convert them to be a function/variable instead
- avoid premature optimization
- code is reference, history and functionality - must be readable as a journal
- be concrete and specific: `retryAfterMs` > `timeout`, `emailValidator` > `validator`
- avoid useless abstractions (functions that mainly call one function, helper used only once)

# Java Fundamentals (Effective Java)
- **Immutability:** Minimize mutability. Prefer `final` fields.
- **Composition > Inheritance:** Prefer composition over inheritance.
- **Encapsulation:** Minimize accessibility (`private` fields, use getters).
- **Interfaces:** Code to the interface, not the implementation (e.g., `List list = new ArrayList<>()`).
- **Contracts:** Always override `hashCode` when overriding `equals`.
- **`try-with-resources`:** Prefer `try-with-resources` for `AutoCloseable` resources.
- **No Raw Types:** Never use raw types (e.g., `List`). Use parameterized types (e.g., `List<String>`).
- **PECS:** Use bounded wildcards (Producer-Extends, Consumer-Super).
- **Method References:** Prefer method references over lambdas where possible.
- **Primitives > Boxed:** Prefer primitive types (`int`) over boxed (`Integer`).
- **`Optional`:** Use `Optional` judiciously for return values; do not use for collections.
- **Executor Framework:** Prefer `java.util.concurrent` over manual `Thread` management.

# Spring and Spring Boot Patterns
- **Constructor Injection:** Use **constructor injection** for dependencies. Declare fields as `final`.
- **`@ConfigurationProperties`:** Prefer type-safe POJOs annotated with `@ConfigurationProperties` over `@Value`.
- **Starters:** Use Starters for simplified dependency management.
- **Java Config:** Prefer `@Configuration` classes and `@Bean` methods over XML.
- **Stereotypes:** Use `@Component`, `@Service`, `@Repository`, `@Controller`.
- **`@RestController`:** Use for REST services with specific mappings (e.g., `@GetMapping`).
- **Spring Data:** Use Repository interfaces to abstract data access.
- **`WebClient`:** Prefer `WebClient` over the legacy `RestTemplate` for service calls.
- **Layers:** Maintain clear separation (Controller, Service, Repository).

# Testing
- **Test Behavior:** Always test behavior, never test implementation.
- **JUnit 5 / Mockito:** Use for unit tests (e.g., testing `Service` logic).
- **Test Slices:** Use `@WebMvcTest` or `@DataJpaTest` to test layers in isolation.
- **Testcontainers:** Use **Testcontainers** for integration tests against real databases/brokers.
- **Assertions:** Use 3rd person verbs in test names (e.g., `userIsCreatedSuccessfully`).
- **Bug Fixes:** Write a test for each bug you fix to ensure no re-occurrence.

# Writing
- be concise, 1 idea per sentence, each word must earn its place
- prefer active voice: "We fixed the bug" over "The bug was fixed by us"
- prefer short sentences
- lead with result, return early, make outcomes obvious
- cut the clutter: delete redundant words in names and code