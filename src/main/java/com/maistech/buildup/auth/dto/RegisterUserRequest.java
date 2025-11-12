package com.maistech.buildup.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record RegisterUserRequest(
    @NotEmpty(message = "Name cannot be empty") String name,
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email must be valid")
    String email,
    @NotEmpty(message = "Password cannot be empty") String password
) {}
