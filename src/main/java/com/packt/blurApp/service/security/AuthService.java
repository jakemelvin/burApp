package com.packt.blurApp.service.security;

import com.packt.blurApp.config.security.JwtService;
import com.packt.blurApp.dto.User.UserSignInDto;
import com.packt.blurApp.exceptions.BadRequestException;
import com.packt.blurApp.exceptions.ConflictException;
import com.packt.blurApp.exceptions.UnauthorizedException;
import com.packt.blurApp.model.Role;
import com.packt.blurApp.model.User;
import com.packt.blurApp.model.enums.PermissionType;
import com.packt.blurApp.model.enums.RoleType;
import com.packt.blurApp.repository.RoleRepository;
import com.packt.blurApp.repository.UserRepository;
import com.packt.blurApp.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse login(UserSignInDto request) {
        log.info("Login attempt for user: {}", request.getUserName());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUserName(),
                            request.getPassword()
                    )
            );

            // Get user details
            User user = (User) authentication.getPrincipal();

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            log.info("User '{}' logged in successfully", user.getUsername());

            // Build roles and permissions (union)
            var allRoles = user.getAllRoles();
            var rolesList = allRoles.stream().map(r -> r.getName().name()).collect(Collectors.toList());
            var permissionsList = allRoles.stream()
                    .flatMap(r -> r.getPermissions().stream())
                    .map(PermissionType::name)
                    .distinct()
                    .collect(Collectors.toList());

            return AuthResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime())
                    .user(AuthResponse.UserInfo.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .role(!rolesList.isEmpty() ? rolesList.get(0) : (user.getRole() != null ? user.getRole().getName().name() : null))
                            .roles(rolesList)
                            .permissions(permissionsList)
                            .build())
                    .build();

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", request.getUserName());
            throw new UnauthorizedException("Invalid username or password");
        }
    }

    @Transactional
    public AuthResponse register(UserSignInDto request, RoleType roleType) {
        log.info("Registration attempt for user: {}", request.getUserName());

        // Check if username already exists
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new ConflictException("Username already exists");
        }

        // Check if email already exists
        if (request.getEmail() != null && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists");
        }

        // Get role
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new BadRequestException("Role not found: " + roleType));

        // Create new user
        User user = User.builder()
                .userName(request.getUserName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User '{}' registered successfully with role '{}'", user.getUsername(), roleType);

        var allRoles = user.getAllRoles();
        var rolesList = allRoles.stream().map(r -> r.getName().name()).collect(Collectors.toList());
        var permissionsList = allRoles.stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(PermissionType::name)
                .distinct()
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .success(true)
                .message("Registration successful")
                .token(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(!rolesList.isEmpty() ? rolesList.get(0) : (user.getRole() != null ? user.getRole().getName().name() : null))
                        .roles(rolesList)
                        .permissions(permissionsList)
                        .build())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refresh token request");

        try {
            // Extract username from refresh token
            String username = jwtService.extractUsername(refreshToken);

            // Load user
            User user = userRepository.findByUserName(username)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            // Validate refresh token
            if (!jwtService.isTokenValid(refreshToken, user)) {
                throw new UnauthorizedException("Invalid refresh token");
            }

            // Generate new access token
            String accessToken = jwtService.generateToken(user);

            log.info("Token refreshed successfully for user: {}", username);

            var allRoles = user.getAllRoles();
            var rolesList = allRoles.stream().map(r -> r.getName().name()).collect(Collectors.toList());
            var permissionsList = allRoles.stream()
                    .flatMap(r -> r.getPermissions().stream())
                    .map(PermissionType::name)
                    .distinct()
                    .collect(Collectors.toList());

            return AuthResponse.builder()
                    .success(true)
                    .message("Token refreshed successfully")
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime())
                    .user(AuthResponse.UserInfo.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .role(!rolesList.isEmpty() ? rolesList.get(0) : (user.getRole() != null ? user.getRole().getName().name() : null))
                            .roles(rolesList)
                            .permissions(permissionsList)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new UnauthorizedException("Invalid refresh token");
        }
    }
}
