package com.maistech.buildup.config;

import lombok.Builder;

@Builder
public record JTWUserData(
        Long userId,
        String email
) {
}
