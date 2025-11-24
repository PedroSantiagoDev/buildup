package com.maistech.buildup.auth.dto;

import java.time.LocalDateTime;

public record PasswordResetResponse(
        String message,
        String email,
        LocalDateTime expiresAt
) {}
