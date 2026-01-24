package com.packt.blurApp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.packt.blurApp.dto.User.AddUserDto;
import com.packt.blurApp.dto.User.UserUpdateDto;
import com.packt.blurApp.mapper.userMapper.UserResponseMapper;
import com.packt.blurApp.model.User;
import com.packt.blurApp.model.enums.RoleType;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.user.IUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ALL_USERS')")
    public ResponseEntity<ApiResponse<?>> getAllUsers() {
        log.info("GET ${api.prefix}/users - Fetch all users");
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully",
                UserResponseMapper.toUserGlobalResponseDtoList(userService.getAllUsers())));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_USER')")
    public ResponseEntity<ApiResponse<?>> createUser(@Valid @RequestBody AddUserDto userDto) {
        log.info("POST ${api.prefix}/users - Create user: {}", userDto.getUserName());
        User createdUser = userService.createUser(userDto);
        return ResponseEntity.ok(ApiResponse.success("User created successfully",
                UserResponseMapper.toUserGlobalResponseDto(createdUser)));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('VIEW_ALL_USERS') or @userService.getCurrentUser().getId() == #userId")
    public ResponseEntity<ApiResponse<?>> getUserById(@PathVariable Long userId) {
        log.info("GET ${api.prefix}/users/{} - Fetch user by ID", userId);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully",
                UserResponseMapper.toUserGlobalResponseDto(userService.getUserById(userId))));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<ApiResponse<?>> updateUser(@PathVariable Long userId, 
                                                      @Valid @RequestBody UserUpdateDto updateDto) {
        log.info("PUT ${api.prefix}/users/{} - Update user", userId);
        User updatedUser = userService.updateUser(userId, updateDto);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully",
                UserResponseMapper.toUserGlobalResponseDto(updatedUser)));
    }

    @PutMapping("/{userId}/profile")
    @PreAuthorize("hasAuthority('UPDATE_OWN_PROFILE') and @userService.getCurrentUser().getId() == #userId")
    public ResponseEntity<ApiResponse<?>> updateUserProfile(@PathVariable Long userId,
                                                             @Valid @RequestBody UserUpdateDto updateDto) {
        log.info("PUT ${api.prefix}/users/{}/profile - Update user profile", userId);
        User updatedUser = userService.updateUserProfile(userId, updateDto);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully",
                UserResponseMapper.toUserGlobalResponseDto(updatedUser)));
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasAuthority('ASSIGN_ROLES')")
    public ResponseEntity<ApiResponse<?>> assignRole(@PathVariable Long userId,
                                                      @RequestParam String role) {
        log.info("PUT ${api.prefix}/users/{}/role - Assign role: {}", userId, role);
        RoleType roleType = RoleType.valueOf(role.toUpperCase());
        User updatedUser = userService.assignRole(userId, roleType);
        return ResponseEntity.ok(ApiResponse.success("Role assigned successfully",
                UserResponseMapper.toUserGlobalResponseDto(updatedUser)));
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('ASSIGN_ROLES')")
    public ResponseEntity<ApiResponse<?>> assignRoles(@PathVariable Long userId,
                                                       @RequestBody List<String> roles) {
        log.info("PUT ${api.prefix}/users/{}/roles - Assign roles: {}", userId, roles);
        Set<RoleType> roleTypes = roles.stream()
                .map(r -> RoleType.valueOf(r.toUpperCase()))
                .collect(Collectors.toSet());
        User updatedUser = userService.assignRoles(userId, roleTypes);
        return ResponseEntity.ok(ApiResponse.success("Roles assigned successfully",
                UserResponseMapper.toUserGlobalResponseDto(updatedUser)));
    }

    @DeleteMapping("/{userId}/roles/{role}")
    @PreAuthorize("hasAuthority('ASSIGN_ROLES')")
    public ResponseEntity<ApiResponse<?>> removeRole(@PathVariable Long userId,
                                                      @PathVariable String role) {
        log.info("DELETE ${api.prefix}/users/{}/roles/{} - Remove role", userId, role);
        RoleType roleType = RoleType.valueOf(role.toUpperCase());
        User updatedUser = userService.removeRole(userId, roleType);
        return ResponseEntity.ok(ApiResponse.success("Role removed successfully",
                UserResponseMapper.toUserGlobalResponseDto(updatedUser)));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('VIEW_ALL_USERS')")
    public ResponseEntity<ApiResponse<?>> getAllRoles() {
        log.info("GET ${api.prefix}/users/roles - Get all available roles");
        List<String> roles = java.util.Arrays.stream(RoleType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Roles fetched successfully", roles));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('DELETE_USER')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long userId) {
        log.info("DELETE ${api.prefix}/users/{} - Delete user", userId);
        userService.deleteUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('VIEW_OWN_PROFILE')")
    public ResponseEntity<ApiResponse<?>> getCurrentUser() {
        log.info("GET ${api.prefix}/users/me - Get current user");
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Current user fetched successfully",
                UserResponseMapper.toUserGlobalResponseDto(currentUser)));
    }
}
