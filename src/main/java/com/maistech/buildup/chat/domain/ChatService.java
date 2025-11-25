package com.maistech.buildup.chat.domain;

import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.auth.domain.UserRepository;
import com.maistech.buildup.chat.ChatMessageEntity;
import com.maistech.buildup.chat.dto.ChatMessageRequest;
import com.maistech.buildup.chat.dto.ChatMessageResponse;
import com.maistech.buildup.project.ProjectEntity;
import com.maistech.buildup.project.domain.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessageResponse sendMessage(UUID senderId, ChatMessageRequest request) {
        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + senderId));

        ProjectEntity project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado: " + request.projectId()));

        validateAccess(project, sender);

        ChatMessageEntity message = ChatMessageEntity.builder()
                .content(request.content())
                .project(project)
                .sender(sender)
                .type(ChatMessageEntity.MessageType.TEXT)
                .build();

        ChatMessageEntity savedMessage = chatMessageRepository.save(message);
        ChatMessageResponse response = toResponse(savedMessage, senderId);

        String destination = "/topic/project." + project.getId();
        messagingTemplate.convertAndSend(destination, response);

        log.info("Mensagem enviada no projeto {} por {}", project.getId(), sender.getEmail());

        return response;
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getHistory(UUID currentUserId, UUID projectId, Pageable pageable) {
        UserEntity currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado"));

        validateAccess(project, currentUser);

        return chatMessageRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(msg -> toResponse(msg, currentUserId));
    }

    private void validateAccess(ProjectEntity project, UserEntity user) {
        boolean isCreator = project.getCreatedBy().getId().equals(user.getId());

        boolean isMember = project.isMember(user.getId());

        boolean isAdmin = user.isAdmin() || user.isSuperAdmin();

        if (!isCreator && !isMember && !isAdmin) {
            log.warn("Tentativa de acesso negado. User: {}, Project: {}", user.getId(), project.getId());
            throw new AccessDeniedException("Você não tem permissão para acessar o chat desta obra.");
        }
    }

    private ChatMessageResponse toResponse(ChatMessageEntity entity, UUID currentUserId) {
        return new ChatMessageResponse(
                entity.getId(),
                entity.getContent(),
                entity.getSender().getId(),
                entity.getSender().getName(),
                entity.getSender().getProfilePhoto(),
                entity.getCreatedAt() != null ? entity.getCreatedAt() : java.time.LocalDateTime.now(),
                entity.getSender().getId().equals(currentUserId)
        );
    }
}
