package com.maistech.buildup.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageResponse(
        UUID id,
        String content,
        UUID senderId,
        String senderName,
        String senderPhoto,
        LocalDateTime sentAt,
        boolean isMine
) {}
