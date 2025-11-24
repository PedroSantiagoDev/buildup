package com.maistech.buildup.auth.dto;

public record VerifyCodeRequest(
        String email,
        String code
) {
}
