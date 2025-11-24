package com.maistech.buildup.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "verification_code", nullable = false, unique = true, length = 10)
    private String verificationCode;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private Boolean used = false;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (expiryDate == null) {
            expiryDate = Instant.now().plus(15, ChronoUnit.MINUTES);
        }
        if (attempts == null) attempts = 0;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }

    public boolean hasExceededAttempts() {
        return this.attempts >= 5;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public PasswordResetTokenEntity(UserEntity user, String verificationCode) {
        this.user = user;
        this.email = user.getEmail();
        this.verificationCode = verificationCode;
        this.expiryDate = Instant.now().plus(15, ChronoUnit.MINUTES);
        this.used = false;
        this.attempts = 0;
    }
}
