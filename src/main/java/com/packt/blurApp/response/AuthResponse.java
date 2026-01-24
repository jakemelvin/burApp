package com.packt.blurApp.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        // Primary role kept for backward compatibility
        private String role;
        // All roles assigned to the user
        private java.util.List<String> roles;
        // Union of all permissions from all roles
        private java.util.List<String> permissions;
    }
}
