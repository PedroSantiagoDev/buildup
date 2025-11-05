package com.maistech.buildup.controller;

import com.maistech.buildup.config.TokenConfig;
import com.maistech.buildup.dto.request.LoginRequest;
import com.maistech.buildup.dto.request.RegisterUserRequest;
import com.maistech.buildup.dto.response.LoginResponse;
import com.maistech.buildup.dto.response.RegisterUserResponse;
import com.maistech.buildup.entity.User;
import com.maistech.buildup.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenConfig tokenConfig;;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var userAndPass = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        var authentication = authenticationManager.authenticate(userAndPass);

        var user = (User) authentication.getPrincipal();
        var token = tokenConfig.generateToken(user);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(@Valid @RequestBody RegisterUserRequest response) {
        var user = new User();

        user.setName(response.name());
        user.setEmail(response.email());
        user.setPassword(passwordEncoder.encode(response.password()));

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterUserResponse(
                user.getName(),
                user.getEmail()
        ));
    }
}
