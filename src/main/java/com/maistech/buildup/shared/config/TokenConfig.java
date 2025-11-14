package com.maistech.buildup.shared.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.maistech.buildup.auth.UserEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class TokenConfig {

    private String secret;
    private long expirationSeconds = 3600;

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setExpirationSeconds(long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(UserEntity user) {
        if (user.getCompany() == null) {
            throw new IllegalStateException("User must belong to a company");
        }

        return JWT.create()
            .withClaim("userId", user.getId().toString())
            .withSubject(user.getEmail())
            .withClaim("companyId", user.getCompany().getId().toString())
            .withClaim("companyName", user.getCompany().getName())
            .withClaim("isMasterCompany", user.getCompany().getIsMaster())
            .withClaim(
                "roles",
                user
                    .getRoles()
                    .stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList())
            )
            .withExpiresAt(Instant.now().plusSeconds(expirationSeconds))
            .withIssuedAt(Instant.now())
            .sign(Algorithm.HMAC256(secret));
    }

    public Optional<JWTUserData> validateToken(String token) {
        try {
            DecodedJWT decoded = JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);

            return Optional.of(
                JWTUserData.builder()
                    .userId(
                        UUID.fromString(decoded.getClaim("userId").asString())
                    )
                    .email(decoded.getSubject())
                    .companyId(
                        UUID.fromString(
                            decoded.getClaim("companyId").asString()
                        )
                    )
                    .companyName(decoded.getClaim("companyName").asString())
                    .isMasterCompany(
                        decoded.getClaim("isMasterCompany").asBoolean()
                    )
                    .roles(decoded.getClaim("roles").asList(String.class))
                    .build()
            );
        } catch (JWTVerificationException ex) {
            return Optional.empty();
        }
    }
}
