package com.maistech.buildup.auth;

import com.maistech.buildup.shared.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenEntity extends BaseEntity {

    @NotNull
    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private UserEntity user;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private Boolean isRevoked = false;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isRevoked && !isExpired();
    }

    public void revoke() {
        this.isRevoked = true;
        this.revokedAt = Instant.now();
    }

    public static RefreshTokenEntity create(UserEntity user, String token, Instant expiresAt) {
        return RefreshTokenEntity.builder()
            .user(user)
            .token(token)
            .expiresAt(expiresAt)
            .companyId(user.getCompany().getId())
            .isRevoked(false)
            .build();
    }
}
