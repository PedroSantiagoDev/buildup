package com.maistech.buildup.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record CreateCompanyRequest(
    @NotEmpty(message = "Company name is required") String name,

    @NotEmpty(message = "Document (CNPJ) is required")
    @Pattern(regexp = "\\d{14}", message = "Document must be 14 digits")
    String document,

    @NotEmpty(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    String phone,
    String address,
    String logoUrl,

    AdminUserRequest adminUser
) {}
