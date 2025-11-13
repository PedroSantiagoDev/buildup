package com.maistech.buildup.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record AdminUserRequest(
    @NotEmpty(message = "Admin name is required") String name,

    @NotEmpty(message = "Admin email is required")
    @Email(message = "Admin email must be valid")
    String email,

    @NotEmpty(message = "Admin password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password
) {}
