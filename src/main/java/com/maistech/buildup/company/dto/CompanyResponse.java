package com.maistech.buildup.company.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyResponse(
    UUID id,
    String name,
    String document,
    String email,
    String phone,
    String address,
    String logoUrl,
    Boolean isActive,
    LocalDateTime createdAt
) {}
