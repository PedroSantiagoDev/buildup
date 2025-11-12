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