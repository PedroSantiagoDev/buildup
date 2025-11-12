package com.maistech.buildup.auth;

import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.dto.LoginResponse;
import com.maistech.buildup.shared.config.TokenConfig;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class LoginUseCase {

    private final AuthenticationManager authenticationManager;
    private final TokenConfig tokenConfig;

    public LoginUseCase(
        AuthenticationManager authenticationManager,
        TokenConfig tokenConfig
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenConfig = tokenConfig;
    }

    public LoginResponse execute(LoginRequest request) {
        var userAndPass = new UsernamePasswordAuthenticationToken(
            request.email(),
            request.password()
        );

        var authentication = authenticationManager.authenticate(userAndPass);
        var user = (UserEntity) authentication.getPrincipal();
        var token = tokenConfig.generateToken(user);

        return new LoginResponse(token, user.getName(), user.getEmail());
    }
}
