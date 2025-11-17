package com.maistech.buildup.auth.domain;

import com.maistech.buildup.auth.*;
import com.maistech.buildup.auth.dto.CreateUserRequest;
import com.maistech.buildup.auth.dto.LoginRequest;
import com.maistech.buildup.auth.dto.LoginResponse;
import com.maistech.buildup.auth.dto.RegisterUserRequest;
import com.maistech.buildup.auth.dto.RegisterUserResponse;
import com.maistech.buildup.auth.dto.UserResponse;
import com.maistech.buildup.auth.exception.InvalidPasswordException;
import com.maistech.buildup.auth.exception.UserAlreadyExistsException;
import com.maistech.buildup.company.CompanyEntity;
import com.maistech.buildup.company.domain.CompanyRepository;
import com.maistech.buildup.role.RoleEntity;
import com.maistech.buildup.role.RoleEnum;
import com.maistech.buildup.role.RoleRepository;
import com.maistech.buildup.shared.config.TokenConfig;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenConfig tokenConfig;

    public AuthService(
        UserRepository userRepository,
        CompanyRepository companyRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        TokenConfig tokenConfig
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
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

        user = userRepository
            .findByIdWithCompanyAndRoles(user.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));

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

        CompanyEntity company = companyRepository
            .findById(request.companyId())
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Company not found: " + request.companyId()
                )
            );

        user.setCompany(company);

        return user;
    }

    public UserResponse createUser(CreateUserRequest request, UUID companyId) {
        validateUserDoesNotExist(request.email());
        validatePasswordStrength(request.password());

        CompanyEntity company = companyRepository
            .findById(companyId)
            .orElseThrow(() ->
                new IllegalArgumentException("Company not found")
            );

        UserEntity user = new UserEntity();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCompany(company);
        user.setIsActive(true);

        if (request.roles() != null && !request.roles().isEmpty()) {
            Set<RoleEntity> rolesToAssign = request
                .roles()
                .stream()
                .map(roleName ->
                    roleRepository
                        .findByName(roleName)
                        .orElseThrow(() ->
                            new IllegalArgumentException(
                                "Role not found: " + roleName
                            )
                        )
                )
                .collect(Collectors.toSet());

            user.assignRoles(rolesToAssign);
        } else {
            RoleEntity userRole = roleRepository
                .findByName(RoleEnum.USER.name())
                .orElseThrow(() ->
                    new IllegalStateException("USER role not found")
                );
            user.assignRole(userRole);
        }

        UserEntity saved = userRepository.save(user);
        return mapToUserResponse(saved);
    }

    private UserResponse mapToUserResponse(UserEntity user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCompany() != null ? user.getCompany().getId() : null,
            user.getCompany() != null ? user.getCompany().getName() : null,
            user
                .getRoles()
                .stream()
                .map(r -> r.getName())
                .collect(Collectors.toList()),
            user.getIsActive(),
            user.getCreatedAt()
        );
    }
}
