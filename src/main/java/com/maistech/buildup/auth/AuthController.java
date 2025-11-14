package com.maistech.buildup.auth;

import com.maistech.buildup.auth.dto.CreateUserRequest;
import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.dto.LoginResponse;
import com.maistech.buildup.auth.dto.RegisterUserRequest;
import com.maistech.buildup.auth.dto.RegisterUserResponse;
import com.maistech.buildup.auth.dto.UserResponse;
import com.maistech.buildup.shared.config.JTWUserData;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
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

    @PostMapping("/users")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody CreateUserRequest request,
        Authentication authentication
    ) {
        JTWUserData userData = (JTWUserData) authentication.getPrincipal();
        UserResponse response = authService.createUser(
            request,
            userData.companyId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
