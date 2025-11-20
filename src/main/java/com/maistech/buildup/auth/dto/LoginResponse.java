package com.maistech.buildup.auth.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    String name,
    String email
) {}
