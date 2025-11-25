package com.maistech.buildup.chat;

import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.chat.domain.ChatService;
import com.maistech.buildup.chat.dto.ChatMessageRequest;
import com.maistech.buildup.chat.dto.ChatMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    @Operation(summary = "Enviar mensagem", description = "Salva a mensagem e notifica via WebSocket")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable UUID projectId,
            @RequestBody @Valid ChatMessageRequest request,
            Authentication authentication
    ) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuário não autenticado.");
        }

        UUID userId = extractUserId(authentication);

        var safeRequest = new ChatMessageRequest(projectId, request.content());

        ChatMessageResponse response = chatService.sendMessage(userId, safeRequest);

        return ResponseEntity.ok(response);
    }

    private UUID extractUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof com.maistech.buildup.shared.security.JWTUserData userData) {
            return userData.userId();
        }

        if (principal instanceof UserEntity user) {
            return user.getId();
        }

        if (principal instanceof String idStr) {
            try {
                return UUID.fromString(idStr);
            } catch (Exception e) {

            }
        }

        throw new ClassCastException("Não foi possível extrair o ID do usuário do tipo: " + principal.getClass().getName());
    }

    @GetMapping
    @Operation(summary = "Listar histórico", description = "Retorna mensagens paginadas (mais recentes primeiro)")
    public ResponseEntity<Page<ChatMessageResponse>> getHistory(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserEntity currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ChatMessageResponse> history = chatService.getHistory(currentUser.getId(), projectId, pageable);
        return ResponseEntity.ok(history);
    }
}
