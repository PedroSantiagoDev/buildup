package com.maistech.buildup.auth.domain;

import com.maistech.buildup.auth.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByToken(String token);

    @Query("SELECT rt FROM RefreshTokenEntity rt WHERE rt.user.id = :userId AND rt.isRevoked = false")
    Optional<RefreshTokenEntity> findActiveByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.isRevoked = true, rt.revokedAt = :revokedAt WHERE rt.user.id = :userId AND rt.isRevoked = false")
    void revokeAllUserTokens(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") Instant now);
}
