package com.maistech.buildup.auth.domain;

import com.maistech.buildup.auth.PasswordResetTokenEntity;
import com.maistech.buildup.auth.UserEntity;
import com.maistech.buildup.auth.dto.ForgotPasswordRequest;
import com.maistech.buildup.auth.dto.ResetPasswordRequest;
import com.maistech.buildup.auth.dto.VerifyCodeRequest;
import com.maistech.buildup.auth.dto.VerifyCodeResponse;
import com.maistech.buildup.auth.utils.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final int MAX_DAILY_REQUESTS = 5;
    private static final int CODE_EXPIRY_MINUTES = 15;
    private static final int MAX_ATTEMPTS = 5;

    @Transactional
    @Async
    public void forgotPassword(ForgotPasswordRequest request) {
        try {
            String email = request.email().toLowerCase().trim();

            Instant oneDayAgo = Instant.now().minus(24, ChronoUnit.HOURS);
            long recentRequests = tokenRepository.countRecentTokensByEmail(email, oneDayAgo);

            if (recentRequests >= MAX_DAILY_REQUESTS) {
                log.warn("Too many password reset requests for email: {}", email);
                return;
            }

            UserEntity user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                log.info("Password reset requested for non-existent email: {}", email);
                return;
            }

            if (!user.getIsActive()) {
                log.info("Password reset requested for inactive user: {}", email);
                return;
            }

            tokenRepository.invalidatePreviousTokens(email);

            String verificationCode = CodeGenerator.generateVerificationCode();

            PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(user, verificationCode);
            tokenRepository.save(resetToken);

            emailService.sendPasswordResetCode(email, verificationCode);

            log.info("Password reset code sent to: {}", email);

        } catch (Exception e) {
            log.error("Error processing password reset request", e);
        }
    }

    @Transactional
    public VerifyCodeResponse verifyCode(VerifyCodeRequest request) {
        String email = request.email().toLowerCase().trim();
        String code = request.code().trim();

        PasswordResetTokenEntity token = tokenRepository
                .findActiveTokenByEmail(email, Instant.now())
                .orElse(null);

        if (token == null) {
            return new VerifyCodeResponse(
                    false,
                    "Nenhum código de verificação ativo encontrado. Solicite um novo código.",
                    null
            );
        }

        if (token.hasExceededAttempts()) {
            token.setUsed(true);
            tokenRepository.save(token);
            return new VerifyCodeResponse(
                    false,
                    "Número máximo de tentativas excedido. Solicite um novo código.",
                    null
            );
        }

        if (!token.getVerificationCode().equals(code)) {
            token.incrementAttempts();
            tokenRepository.save(token);

            int remainingAttempts = MAX_ATTEMPTS - token.getAttempts();
            String message = remainingAttempts > 0
                    ? String.format("Código incorreto. Você tem %d tentativa(s) restante(s).", remainingAttempts)
                    : "Número máximo de tentativas excedido. Solicite um novo código.";

            return new VerifyCodeResponse(false, message, null);
        }

        log.info("Code verified successfully for email: {}", email);

        return new VerifyCodeResponse(
                true,
                "Código verificado com sucesso!",
                token.getId().toString()
        );
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Password reset attempt with token: {}", request.resetToken());

        if (request.resetToken() == null || request.resetToken().trim().isEmpty()) {
            throw new IllegalArgumentException("Token de reset é obrigatório");
        }

        if (request.newPassword() == null || request.newPassword().length() < 6) {
            throw new IllegalArgumentException("A nova senha deve ter pelo menos 6 caracteres");
        }

        UUID tokenId;
        try {
            tokenId = UUID.fromString(request.resetToken());
            log.info("Token converted to UUID: {}", tokenId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid token format: {}", request.resetToken());
            throw new RuntimeException("Token de reset inválido");
        }

        PasswordResetTokenEntity resetToken = tokenRepository
                .findValidTokenById(tokenId, Instant.now())
                .orElseThrow(() -> {
                    log.error("Token not found or invalid: {}", tokenId);
                    return new RuntimeException("Token inválido ou expirado");
                });

        UserEntity user = resetToken.getUser();
        if (user == null) {
            log.error("User not found for token: {}", tokenId);
            throw new RuntimeException("Usuário não encontrado");
        }

        log.info("Updating password for user: {}", user.getEmail());

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        try {
            emailService.sendPasswordChangedConfirmation(user.getEmail());
        } catch (Exception e) {
            log.error("Error sending confirmation email: {}", e.getMessage());
        }

        log.info("Password successfully reset for user: {}", user.getEmail());
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(Instant.now());
        log.info("Expired password reset tokens cleaned up");
    }

    public boolean hasActiveCode(String email) {
        return tokenRepository
                .findActiveTokenByEmail(email.toLowerCase().trim(), Instant.now())
                .isPresent();
    }

    @Transactional
    public void cancelActiveCode(String email) {
        tokenRepository.invalidatePreviousTokens(email.toLowerCase().trim());
        log.info("Active recovery codes cancelled for: {}", email);
    }
}