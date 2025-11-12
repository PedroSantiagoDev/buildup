package com.maistech.buildup.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.maistech.buildup.auth.dto.RegisterUserRequest;
import com.maistech.buildup.auth.dto.RegisterUserResponse;
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
class RegisterUserIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        "postgres:16-alpine"
    )
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

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
    void shouldRegisterUserSuccessfully() {
        RegisterUserRequest request = new RegisterUserRequest(
            "John Doe",
            "john@example.com",
            "password123"
        );

        ResponseEntity<RegisterUserResponse> response =
            restTemplate.postForEntity(
                "/auth/register",
                request,
                RegisterUserResponse.class
            );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("John Doe");
        assertThat(response.getBody().email()).isEqualTo("john@example.com");

        assertThat(userRepository.findByEmail("john@example.com")).isPresent();
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() {
        RegisterUserRequest firstRequest = new RegisterUserRequest(
            "John Doe",
            "john@example.com",
            "password123"
        );
        restTemplate.postForEntity(
            "/auth/register",
            firstRequest,
            RegisterUserResponse.class
        );

        RegisterUserRequest duplicateRequest = new RegisterUserRequest(
            "Jane Doe",
            "john@example.com",
            "password456"
        );
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/auth/register",
            duplicateRequest,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturnBadRequestWhenInvalidEmail() {
        RegisterUserRequest request = new RegisterUserRequest(
            "John Doe",
            "invalid-email",
            "password123"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/auth/register",
            request,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
