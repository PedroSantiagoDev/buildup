package com.maistech.buildup.chat.domain;

import com.maistech.buildup.chat.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {
    Page<ChatMessageEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);
}
