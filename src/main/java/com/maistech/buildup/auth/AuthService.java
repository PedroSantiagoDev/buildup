package com.maistech.buildup.auth;

import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.dto.LoginResponse;
import com.maistech.buildup.auth.dto.RegisterUserRequest;
import com.maistech.buildup.auth.dto.RegisterUserResponse;
import com.maistech.buildup.auth.exception.InvalidPasswordException;
import com.maistech.buildup.auth.exception.UserAlreadyExistsException;
import com.maistech.buildup.shared.config.TokenConfig;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenConfig tokenConfig;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        TokenConfig tokenConfig
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenConfig = tokenConfig;
    }

    public LoginResponse login(LoginRequest request) {
        var credentials = new UsernamePasswordAuthenticationToken(
            request.email(),
            request.password()
        );

        var authentication = authenticationManager.authenticate(credentials);
        var user = (UserEntity) authentication.getPrincipal();
        var token = tokenConfig.generateToken(user);

        return new LoginResponse(token, user.getName(), user.getEmail());
    }

    public RegisterUserResponse register(RegisterUserRequest request) {
        validateUserDoesNotExist(request.email());
        validatePasswordStrength(request.password());

        var user = createUserFromRequest(request);
        var savedUser = userRepository.save(user);

        return new RegisterUserResponse(
            savedUser.getName(),
            savedUser.getEmail()
        );
    }

    private void validateUserDoesNotExist(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(
                "User with email " + email + " already exists"
            );
        }
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new InvalidPasswordException(
                "Password must be at least 8 characters"
            );
        }
    }

    private UserEntity createUserFromRequest(RegisterUserRequest request) {
        var user = new UserEntity();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        return user;
    }
}
