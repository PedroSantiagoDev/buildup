package com.maistech.buildup.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.domain.*;
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

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        RegisterUserRequest registerRequest = new RegisterUserRequest(
            "John Doe",
            "john@example.com",
            "password123",
            null
        );
        restTemplate.postForEntity(
            "/auth/register",
            registerRequest,
            Object.class
        );

        LoginRequest loginRequest = new LoginRequest(
            "john@example.com",
            "password123"
        );

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
