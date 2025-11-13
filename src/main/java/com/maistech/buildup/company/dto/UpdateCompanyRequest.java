package com.maistech.buildup.company.dto;

import jakarta.validation.constraints.Email;

public record UpdateCompanyRequest(
    String name,

    @Email(message = "Email must be valid") String email,

    String phone,
    String address,
    String logoUrl
) {}
