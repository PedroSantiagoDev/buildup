package com.maistech.buildup.auth.dto;

import jakarta.validation.constraints.NotEmpty;

public record LoginRequest(
        @NotEmpty(message = "Email must not be empty")
        String email,
        @NotEmpty(message = "Password must not be empty")
        String password
) {
}
