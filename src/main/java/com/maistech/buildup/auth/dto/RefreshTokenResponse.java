package com.maistech.buildup.auth.dto;

public record RefreshTokenResponse(
    String accessToken,
    String refreshToken
) {}
