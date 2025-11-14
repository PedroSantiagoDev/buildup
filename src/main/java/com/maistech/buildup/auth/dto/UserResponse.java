package com.maistech.buildup.auth.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String email,
    UUID companyId,
    String companyName,
    List<String> roles,
    Boolean isActive,
    LocalDateTime createdAt
) {}
