package com.packt.blurApp.dto.User;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSignInResponseDto {
    private Long id;
    private String userName;
    private String email;
    private String role;
    private Set<String> permissions = new HashSet<>();
}
