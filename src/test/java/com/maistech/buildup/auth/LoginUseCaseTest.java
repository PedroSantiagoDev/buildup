package com.maistech.buildup.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maistech.buildup.auth.dto.LoginRequest;
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

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenConfig tokenConfig;

    @InjectMocks
    private LoginUseCase loginUseCase;

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        LoginRequest request = new LoginRequest(
            "john@example.com",
            "password123"
        );

        UserEntity user = new UserEntity();
        user.setEmail("john@example.com");
        user.setName("John Doe");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user,
            null
        );
        when(authenticationManager.authenticate(any())).thenReturn(
            authentication
        );
        when(tokenConfig.generateToken(user)).thenReturn("mock-jwt-token");

        var response = loginUseCase.execute(request);

        assertThat(response.token()).isEqualTo("mock-jwt-token");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.name()).isEqualTo("John Doe");

        verify(authenticationManager).authenticate(any());
        verify(tokenConfig).generateToken(user);
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

        assertThatThrownBy(() -> loginUseCase.execute(request)).isInstanceOf(
            BadCredentialsException.class
        );

        verify(authenticationManager).authenticate(any());
    }
}
