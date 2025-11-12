package com.maistech.buildup.shared.config;

import java.util.UUID;
import lombok.Builder;

@Builder
public record JTWUserData(UUID userId, String email) {}
