package com.maistech.buildup.auth;

import com.maistech.buildup.auth.domain.PasswordResetService;
import com.maistech.buildup.auth.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    @Operation(summary = "Solicitar código de recuperação",
            description = "Envia um código de verificação para o email informado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Requisição processada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Email inválido"),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas")
    })
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Password recovery code requested for: {}", request.email());

        passwordResetService.forgotPassword(request);

        return ResponseEntity.ok(MessageResponse.success(
                "Se o email informado estiver cadastrado, você receberá um código de verificação em instantes."
        ));
    }

    @PostMapping("/verify-code")
    @Operation(summary = "Verificar código",
            description = "Valida o código de verificação enviado por email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Código validado"),
            @ApiResponse(responseCode = "400", description = "Código inválido ou expirado")
    })
    public ResponseEntity<VerifyCodeResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        log.info("Code verification attempt for: {}", request.email());

        VerifyCodeResponse response = passwordResetService.verifyCode(request);

        if (!response.valid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    @Operation(summary = "Redefinir senha",
            description = "Define uma nova senha após validação do código")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou senha fraca"),
            @ApiResponse(responseCode = "401", description = "Token expirado")
    })
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset attempt with validated token");

        try {
            passwordResetService.resetPassword(request);
            return ResponseEntity.ok(MessageResponse.success(
                    "Senha alterada com sucesso! Você já pode fazer login com sua nova senha."
            ));
        } catch (RuntimeException e) {
            log.error("Password reset failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/has-active-code")
    @Operation(summary = "Verificar código ativo",
            description = "Verifica se existe um código de recuperação ativo para o email")
    public ResponseEntity<MessageResponse> hasActiveCode(@RequestParam String email) {
        boolean hasActive = passwordResetService.hasActiveCode(email);

        return ResponseEntity.ok(MessageResponse.success(
                hasActive ? "Existe um código ativo" : "Nenhum código ativo"
        ));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancelar recuperação",
            description = "Cancela o processo de recuperação de senha em andamento")
    public ResponseEntity<MessageResponse> cancelRecovery(@RequestBody ForgotPasswordRequest request) {
        log.info("Password recovery cancelled for: {}", request.email());

        passwordResetService.cancelActiveCode(request.email());

        return ResponseEntity.ok(MessageResponse.success(
                "Processo de recuperação cancelado com sucesso."
        ));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica se o serviço está funcionando")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(MessageResponse.success("Password reset service is running"));
    }
}
