package com.maistech.buildup.chat;

import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.project.ProjectEntity;
import com.maistech.buildup.shared.entity.BaseEntity;
import com.maistech.buildup.tenant.CompanyEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "project_chat_messages")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEntity extends BaseEntity {

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        SYSTEM_ALERT
    }
}
