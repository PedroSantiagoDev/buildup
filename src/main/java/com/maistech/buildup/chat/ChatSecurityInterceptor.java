package com.maistech.buildup.chat;

import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.auth.config.TokenConfig;
import com.maistech.buildup.auth.domain.UserRepository;
import com.maistech.buildup.project.ProjectEntity;
import com.maistech.buildup.project.domain.ProjectRepository;
import com.maistech.buildup.shared.security.JWTUserData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSecurityInterceptor implements ChannelInterceptor {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TokenConfig tokenConfig;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );

        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Optional<JWTUserData> userDataOpt = tokenConfig.validateToken(
                            token
                    );

                    if (userDataOpt.isPresent()) {
                        JWTUserData userData = userDataOpt.get();

                        String email = userData.email();

                        log.debug("Token WebSocket válido. Email: {}", email);

                        UserEntity user = userRepository
                                .findByEmailWithRoles(email)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Usuário não encontrado: " + email
                                        )
                                );

                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities()
                        );

                        accessor.setUser(auth);
                        log.info(
                                "WebSocket Autenticado com sucesso: {}",
                                email
                        );
                    } else {
                        log.warn("Token WebSocket inválido ou expirado.");
                    }
                } catch (Exception e) {
                    log.error(
                            "Falha na autenticação WebSocket: {}",
                            e.getMessage()
                    );
                }
            }
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            Authentication userAuth = (Authentication) accessor.getUser();

            if (userAuth == null) {
                throw new AccessDeniedException(
                        "Usuário não autenticado. Envie o Token JWT no CONNECT."
                );
            }

            if (
                    destination != null &&
                            destination.startsWith("/topic/project.")
            ) {
                String projectIdStr = destination.substring(
                        "/topic/project.".length()
                );
                try {
                    UUID projectId = UUID.fromString(projectIdStr);
                    UserEntity user = (UserEntity) userAuth.getPrincipal();

                    validateSubscriptionAccess(projectId, user);

                    log.info(
                            "Acesso PERMITIDO ao chat da obra {} para {}",
                            projectId,
                            user.getEmail()
                    );
                } catch (IllegalArgumentException e) {
                    throw new AccessDeniedException(
                            "Formato de UUID inválido no tópico"
                    );
                }
            }
        }
        return message;
    }

    private void validateSubscriptionAccess(UUID projectId, UserEntity user) {

        ProjectEntity project = projectRepository
                .findById(projectId)
                .orElseThrow(() -> new AccessDeniedException("Projeto não encontrado"));

        boolean isCreator =
                project.getCreatedBy() != null &&
                        project.getCreatedBy().getId().equals(user.getId());

        boolean isAdmin = user.isAdmin() || user.isSuperAdmin();

        boolean isMember = projectRepository.isUserMember(
                projectId,
                user.getId()
        );

        if (!isMember && !isCreator && !isAdmin) {
            log.warn(
                    "Acesso NEGADO. Usuário {} tentou acessar obra {}",
                    user.getEmail(),
                    projectId
            );
            throw new AccessDeniedException(
                    "Você não tem permissão para acessar o chat desta obra."
            );
        }
    }
}