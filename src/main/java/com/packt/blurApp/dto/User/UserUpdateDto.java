package com.packt.blurApp.dto.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDto {
    
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String userName;
    
    @Email(message = "Email should be valid")
    private String email;
    
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    private String currentPassword;
    
    // Support for updating multiple roles
    private Set<String> roles;
    
    // Account status fields for admin to update
    private Boolean enabled;
    private Boolean accountNonLocked;
}
