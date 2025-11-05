package com.maistech.buildup.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.maistech.buildup.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class TokenConfig {

    private final String secret = "maistech";

    // Token expira em 1 hora (3600 segundos)
    private static final long TOKEN_EXPIRATION_SECONDS = 3600;

    public String generateToken(UserEntity user) {
        return JWT
                .create()
                .withClaim("userId", user.getId().toString())
                .withSubject(user.getEmail())
                .withExpiresAt(Instant.now().plusSeconds(TOKEN_EXPIRATION_SECONDS))
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
