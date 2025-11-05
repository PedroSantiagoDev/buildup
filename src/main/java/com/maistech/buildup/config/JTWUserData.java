package com.maistech.buildup.config;

import lombok.Builder;

import java.util.UUID;

@Builder
public record JTWUserData(
        UUID userId,
        String email
) {
}
