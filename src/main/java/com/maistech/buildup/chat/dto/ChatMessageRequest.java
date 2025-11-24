package com.maistech.buildup.chat.dto;

import java.util.UUID;

public record ChatMessageRequest(
        UUID projectId,
        String content
) {}
