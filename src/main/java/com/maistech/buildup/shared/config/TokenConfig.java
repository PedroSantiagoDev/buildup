package com.maistech.buildup.shared.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.maistech.buildup.auth.UserEntity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

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
        return JWT
                .create()
                .withClaim("userId", user.getId().toString())
                .withSubject(user.getEmail())
                .withExpiresAt(Instant.now().plusSeconds(expirationSeconds))
                .withIssuedAt(Instant.now())
                .sign(Algorithm.HMAC256(secret));
    }

    public Optional<JTWUserData> validateToken(String token) {
        try {
            var algorithm = Algorithm.HMAC256(secret);

            DecodedJWT decode = JWT
                    .require(algorithm)
                    .build()
                    .verify(token);

            return Optional
                    .of(JTWUserData
                            .builder()
                            .userId(UUID.fromString(decode.getClaim("userId").asString()))
                            .email(decode.getSubject())
                            .build());
        } catch (JWTVerificationException ex) {
            return Optional.empty();
        }
    }
}
