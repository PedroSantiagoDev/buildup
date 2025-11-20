package com.maistech.buildup.auth;

import com.maistech.buildup.auth.dto.CreateUserRequest;
import com.maistech.buildup.auth.domain.AuthService;
import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.dto.LoginResponse;
import com.maistech.buildup.auth.dto.RefreshTokenRequest;
import com.maistech.buildup.auth.dto.RefreshTokenResponse;
import com.maistech.buildup.auth.dto.RegisterUserRequest;
import com.maistech.buildup.auth.dto.RegisterUserResponse;
import com.maistech.buildup.auth.dto.UserResponse;
import com.maistech.buildup.shared.security.JWTUserData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/auth")
@Tag(
    name = "Authentication",
    description = "User authentication and registration endpoints"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user and receive JWT token"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully authenticated",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid credentials",
                content = @Content
            ),
        }
    )
    public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(
        summary = "User registration",
        description = "Register new user with company ID (public registration with invite code)"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "User successfully created",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                        implementation = RegisterUserResponse.class
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error or user already exists",
                content = @Content
            ),
        }
    )
    public ResponseEntity<RegisterUserResponse> register(
        @Valid @RequestBody RegisterUserRequest request
    ) {
        RegisterUserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/users")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
        summary = "Create user in company",
        description = "Admin creates new user in their company. User is automatically linked to admin's company from JWT token."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "User successfully created",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - invalid or missing token",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - user doesn't have required role",
                content = @Content
            ),
        }
    )
    public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody CreateUserRequest request,
        Authentication authentication
    ) {
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();
        UserResponse response = authService.createUser(
            request,
            userData.companyId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Generate new access token and refresh token using a valid refresh token"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Tokens refreshed successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RefreshTokenResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid or expired refresh token",
                content = @Content
            ),
        }
    )
    public ResponseEntity<RefreshTokenResponse> refresh(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        RefreshTokenResponse response = authService.refreshAccessToken(
            request.refreshToken()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
        summary = "User logout",
        description = "Logout user by revoking all active refresh tokens"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "204",
                description = "Successfully logged out"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - invalid or missing token",
                content = @Content
            ),
        }
    )
    public ResponseEntity<Void> logout(Authentication authentication) {
        JWTUserData userData = (JWTUserData) authentication.getPrincipal();
        authService.logout(userData.userId());
        return ResponseEntity.noContent().build();
    }
}
