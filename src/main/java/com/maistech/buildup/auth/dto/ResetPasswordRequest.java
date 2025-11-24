package com.maistech.buildup.auth.dto;

public record ResetPasswordRequest(
        String resetToken,
        String newPassword
) {}
