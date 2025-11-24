package com.maistech.buildup.auth.dto;

public record VerifyCodeResponse(
        boolean valid,
        String message,
        String resetToken
) {
}
