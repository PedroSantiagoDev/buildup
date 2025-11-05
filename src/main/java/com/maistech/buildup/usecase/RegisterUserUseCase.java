package com.maistech.buildup.usecase;

import com.maistech.buildup.dto.request.RegisterUserRequest;
import com.maistech.buildup.dto.response.RegisterUserResponse;
import com.maistech.buildup.entity.UserEntity;
import com.maistech.buildup.exception.UserAlreadyExistsException;
import com.maistech.buildup.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterUserResponse execute(RegisterUserRequest request) {
        validateUserDoesNotExist(request.email());

        var user = createUserFromRequest(request);
        var savedUser = userRepository.save(user);

        return new RegisterUserResponse(
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    private void validateUserDoesNotExist(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
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

