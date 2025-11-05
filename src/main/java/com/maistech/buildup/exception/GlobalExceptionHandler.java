package com.maistech.buildup.exception;

import com.maistech.buildup.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralizador de tratamento de exceções da aplicação.
 *
 * @RestControllerAdvice intercepta TODAS as exceções lançadas pelos controllers
 * e retorna respostas HTTP padronizadas ao cliente.
 * <p>
 * Fluxo: Controller → Exception → GlobalExceptionHandler → Response formatada
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata tentativas de registro com email já existente.
     * <p>
     * Cenário: POST /auth/register com email duplicado
     * Retorna: 409 CONFLICT
     * <p>
     * Exemplo de resposta:
     * {
     * "status": 409,
     * "message": "User with email user@example.com already exists",
     * "timestamp": "2025-11-05T15:30:00"
     * }
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Trata erros de autenticação (credenciais inválidas).
     * <p>
     * Cenário: POST /auth/login com senha errada ou email inexistente
     * Retorna: 401 UNAUTHORIZED
     * <p>
     * Exemplo de resposta:
     * {
     * "status": 401,
     * "message": "Invalid email or password",
     * "timestamp": "2025-11-05T15:30:00"
     * }
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid email or password",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Trata erros de validação de campos (@Valid nos DTOs).
     * <p>
     * Cenário: Campos não passam nas validações @NotBlank, @Email, @Size, etc.
     * Retorna: 400 BAD REQUEST com mapa de erros por campo
     * <p>
     * Exemplo de resposta:
     * {
     * "status": 400,
     * "errors": {
     * "email": "must be a well-formed email address",
     * "password": "size must be between 6 and 100"
     * },
     * "timestamp": "2025-11-05T15:30:00"
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        // Extrai todos os erros de validação e agrupa por campo
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Tratamento genérico para qualquer exceção não prevista.
     * <p>
     * Funciona como "rede de segurança" - captura erros inesperados
     * (ex: banco de dados fora, NullPointerException, etc.)
     * <p>
     * Retorna: 500 INTERNAL SERVER ERROR
     * <p>
     * IMPORTANTE: Em produção, NÃO exponha detalhes do erro (ex.getMessage())
     * para evitar vazamento de informações sensíveis.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

