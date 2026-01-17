package com.packt.blurApp.service.user;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packt.blurApp.dto.User.AddUserDto;
import com.packt.blurApp.dto.User.UserUpdateDto;
import com.packt.blurApp.exceptions.BadRequestException;
import com.packt.blurApp.exceptions.ConflictException;
import com.packt.blurApp.exceptions.ForbiddenException;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.exceptions.UnauthorizedException;
import com.packt.blurApp.model.Role;
import com.packt.blurApp.model.User;
import com.packt.blurApp.model.enums.RoleType;
import com.packt.blurApp.repository.RoleRepository;
import com.packt.blurApp.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(Long userId) {
        log.debug("Fetching user by ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundExceptions("User not found with ID: " + userId));
    }

    @Override
    @Transactional
    public User createUser(AddUserDto addUserDto) {
        log.info("Creating new user: {}", addUserDto.getUserName());
        
        // Check if username already exists
        if (userRepository.existsByUserName(addUserDto.getUserName())) {
            throw new ConflictException("Username already exists: " + addUserDto.getUserName());
        }
        
        // Check if email already exists
        if (addUserDto.getEmail() != null && userRepository.existsByEmail(addUserDto.getEmail())) {
            throw new ConflictException("Email already exists: " + addUserDto.getEmail());
        }
        
        // Parse role
        RoleType roleType;
        try {
            roleType = RoleType.valueOf(addUserDto.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + addUserDto.getRole());
        }
        
        // Get role entity
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new BadRequestException("Role not found: " + roleType));
        
        // Create user
        User newUser = User.builder()
                .userName(addUserDto.getUserName())
                .email(addUserDto.getEmail())
                .password(passwordEncoder.encode(addUserDto.getPassword()))
                .role(role)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        User savedUser = userRepository.save(newUser);
        log.info("User created successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    @Override
    @Transactional
    public User updateUser(Long userId, UserUpdateDto updateDto) {
        log.info("Updating user: {}", userId);
        
        User user = getUserById(userId);
        
        // Update username if provided
        if (updateDto.getUserName() != null && !updateDto.getUserName().equals(user.getUsername())) {
            if (userRepository.existsByUserName(updateDto.getUserName())) {
                throw new ConflictException("Username already exists: " + updateDto.getUserName());
            }
            user.setUserName(updateDto.getUserName());
        }
        
        // Update email if provided
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new ConflictException("Email already exists: " + updateDto.getEmail());
            }
            user.setEmail(updateDto.getEmail());
        }
        
        // Update password if provided
        if (updateDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", userId);
        return updatedUser;
    }

    @Override
    @Transactional
    public User updateUserProfile(Long userId, UserUpdateDto updateDto) {
        log.info("Updating user profile: {}", userId);
        
        User currentUser = getCurrentUser();
        
        // Users can only update their own profile
        if (!currentUser.getId().equals(userId)) {
            throw new ForbiddenException("You can only update your own profile");
        }
        
        // Verify current password if changing password
        if (updateDto.getPassword() != null) {
            if (updateDto.getCurrentPassword() == null) {
                throw new BadRequestException("Current password is required to change password");
            }
            
            if (!passwordEncoder.matches(updateDto.getCurrentPassword(), currentUser.getPassword())) {
                throw new BadRequestException("Current password is incorrect");
            }
        }
        
        return updateUser(userId, updateDto);
    }

    @Override
    @Transactional
    public User assignRole(Long userId, RoleType roleType) {
        log.info("Assigning role {} to user {}", roleType, userId);
        
        User user = getUserById(userId);
        
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new BadRequestException("Role not found: " + roleType));
        
        user.setRole(role);
        User updatedUser = userRepository.save(user);
        
        log.info("Role assigned successfully to user {}", userId);
        return updatedUser;
    }

    @Override
    @Transactional
    public void deleteUserById(Long userId) {
        log.info("Deleting user: {}", userId);
        
        User userToDelete = getUserById(userId);
        
        // Prevent deleting the last GREAT_ADMIN
        if (userToDelete.getRole().getName() == RoleType.GREAT_ADMIN) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRole().getName() == RoleType.GREAT_ADMIN)
                    .count();
            
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot delete the last GREAT_ADMIN user");
            }
        }
        
        userRepository.delete(userToDelete);
        log.info("User deleted successfully: {}", userId);
    }

    @Override
    public List<User> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        }
        
        throw new UnauthorizedException("Invalid authentication principal");
    }
}
