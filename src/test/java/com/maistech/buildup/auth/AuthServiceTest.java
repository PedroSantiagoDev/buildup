package com.maistech.buildup.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.maistech.buildup.auth.domain.AuthService;
import com.maistech.buildup.auth.domain.UserRepository;
import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.dto.RegisterUserRequest;
import com.maistech.buildup.auth.exception.InvalidPasswordException;
import com.maistech.buildup.auth.exception.UserAlreadyExistsException;
import com.maistech.buildup.auth.config.TokenConfig;
import com.maistech.buildup.tenant.CompanyEntity;
import com.maistech.buildup.tenant.CompanyRepository;
import com.maistech.buildup.role.RoleRepository;
import java.util.Optional;
import java.util.UUID;
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
    private CompanyRepository companyRepository;

    @Mock
    private RoleRepository roleRepository;

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
        LoginRequest request = new LoginRequest(
            "john@example.com",
            "password123"
        );

        UUID userId = UUID.randomUUID();
        UserEntity authenticatedUser = new UserEntity();
        authenticatedUser.setEmail("john@example.com");
        authenticatedUser.setName("John Doe");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            authenticatedUser,
            null
        );
        when(authenticationManager.authenticate(any())).thenReturn(
            authentication
        );

        UserEntity userWithDetails = new UserEntity();
        userWithDetails.setEmail("john@example.com");
        userWithDetails.setName("John Doe");

        when(userRepository.findByIdWithCompanyAndRoles(null))
            .thenReturn(Optional.of(userWithDetails));
        when(tokenConfig.generateToken(userWithDetails)).thenReturn(
            "mock-jwt-token"
        );

        var response = authService.login(request);

        assertThat(response.token()).isEqualTo("mock-jwt-token");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.name()).isEqualTo("John Doe");

        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByIdWithCompanyAndRoles(null);
        verify(tokenConfig).generateToken(userWithDetails);
    }

    @Test
    void shouldThrowExceptionWhenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest(
            "john@example.com",
            "wrongpassword"
        );

        when(authenticationManager.authenticate(any())).thenThrow(
            new BadCredentialsException("Bad credentials")
        );

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(
            BadCredentialsException.class
        );

        verify(authenticationManager).authenticate(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterUserRequest request = new RegisterUserRequest(
            "John Doe",
            "john@example.com",
            "password123",
            null
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
        UUID companyId = UUID.randomUUID();
        RegisterUserRequest request = new RegisterUserRequest(
            "John Doe",
            "john@example.com",
            "password123",
            companyId
        );

        CompanyEntity company = new CompanyEntity();
        company.setId(companyId);
        company.setName("Test Company");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(
            false
        );
        when(companyRepository.findById(companyId)).thenReturn(
            Optional.of(company)
        );
        when(passwordEncoder.encode("password123")).thenReturn(
            "encodedPassword"
        );

        UserEntity savedUser = new UserEntity();
        savedUser.setName("John Doe");
        savedUser.setEmail("john@example.com");
        savedUser.setPassword("encodedPassword");

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        var response = authService.register(request);

        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.email()).isEqualTo("john@example.com");

        verify(userRepository).existsByEmail("john@example.com");
        verify(companyRepository).findById(companyId);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsTooShort() {
        RegisterUserRequest request = new RegisterUserRequest(
            "John Doe",
            "john@example.com",
            "short",
            null
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(
            false
        );

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("at least 8 characters");

        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any());
    }
}
