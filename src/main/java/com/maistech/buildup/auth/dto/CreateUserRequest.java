package com.maistech.buildup.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record CreateUserRequest(
    @NotEmpty(message = "Name is required") String name,
    
    @NotEmpty(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,
    
    @NotEmpty(message = "Password is required") String password,
    
    List<String> roles
) {}
