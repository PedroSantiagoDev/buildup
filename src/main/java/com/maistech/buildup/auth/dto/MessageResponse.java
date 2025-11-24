package com.maistech.buildup.auth.dto;

public record MessageResponse(
        boolean success,
        String message
) {
    public static MessageResponse success(String message) {
        return new MessageResponse(true, message);
    }

    public static MessageResponse error(String message) {
        return new MessageResponse(false, message);
    }
}
