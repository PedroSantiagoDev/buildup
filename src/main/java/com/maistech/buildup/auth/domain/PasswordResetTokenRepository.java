package com.maistech.buildup.auth.domain;

import com.maistech.buildup.auth.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    @Query("SELECT t FROM PasswordResetTokenEntity t WHERE t.email = :email " +
            "AND t.verificationCode = :code " +
            "AND t.used = false " +
            "AND t.expiryDate > :now " +
            "AND t.attempts < 5")
    Optional<PasswordResetTokenEntity> findValidTokenByEmailAndCode(
            @Param("email") String email,
            @Param("code") String code,
            @Param("now") Instant now
    );

    @Query("SELECT t FROM PasswordResetTokenEntity t WHERE t.id = :tokenId " +
            "AND t.used = false " +
            "AND t.expiryDate > :now")
    Optional<PasswordResetTokenEntity> findValidTokenById(
            @Param("tokenId") UUID tokenId,
            @Param("now") Instant now
    );

    @Query("SELECT t FROM PasswordResetTokenEntity t WHERE t.email = :email " +
            "AND t.used = false " +
            "AND t.expiryDate > :now")
    Optional<PasswordResetTokenEntity> findActiveTokenByEmail(
            @Param("email") String email,
            @Param("now") Instant now
    );

    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.expiryDate < :now OR t.used = true")
    void deleteExpiredTokens(@Param("now") Instant now);

    @Modifying
    @Query("UPDATE PasswordResetTokenEntity t SET t.used = true WHERE t.email = :email AND t.used = false")
    void invalidatePreviousTokens(@Param("email") String email);

    @Query("SELECT COUNT(t) FROM PasswordResetTokenEntity t WHERE t.email = :email AND t.createdAt > :since")
    long countRecentTokensByEmail(@Param("email") String email, @Param("since") Instant since);
}