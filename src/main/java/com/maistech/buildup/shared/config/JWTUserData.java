package com.maistech.buildup.shared.config;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record JWTUserData(
    UUID userId,
    String email,
    UUID companyId,
    String companyName,
    Boolean isMasterCompany,
    List<String> roles
) {}
