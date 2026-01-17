package com.packt.blurApp.controller.auth;

import com.packt.blurApp.dto.User.UserSignInDto;
import com.packt.blurApp.model.enums.RoleType;
import com.packt.blurApp.response.AuthResponse;
import com.packt.blurApp.service.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserSignInDto request) {
        log.info("POST /api/auth/login - Login request for user: {}", request.getUserName());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserSignInDto request) {
        log.info("POST /api/auth/register - Registration request for user: {}", request.getUserName());
        // By default, new users are registered as RACER
        AuthResponse response = authService.register(request, RoleType.RACER);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        log.info("POST /api/auth/refresh - Token refresh request");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        
        String refreshToken = authHeader.substring(7);
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}
