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
        
        // Create user first
        User newUser = User.builder()
                .userName(addUserDto.getUserName())
                .email(addUserDto.getEmail())
                .password(passwordEncoder.encode(addUserDto.getPassword()))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        // Handle multiple roles
        if (addUserDto.getRoles() != null && !addUserDto.getRoles().isEmpty()) {
            for (String roleStr : addUserDto.getRoles()) {
                try {
                    RoleType roleType = RoleType.valueOf(roleStr.toUpperCase());
                    Role role = roleRepository.findByName(roleType)
                            .orElseThrow(() -> new BadRequestException("Role not found: " + roleType));
                    newUser.addRole(role);
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid role: " + roleStr);
                }
            }
        } else if (addUserDto.getRole() != null) {
            // Fallback to legacy single role
            try {
                RoleType roleType = RoleType.valueOf(addUserDto.getRole().toUpperCase());
                Role role = roleRepository.findByName(roleType)
                        .orElseThrow(() -> new BadRequestException("Role not found: " + roleType));
                newUser.addRole(role);
                newUser.setRole(role); // Keep legacy field for backward compatibility
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role: " + addUserDto.getRole());
            }
        } else {
            throw new BadRequestException("At least one role is required");
        }
        
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
        if (updateDto.getPassword() != null && !updateDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }
        
        // Update roles if provided (admin operation)
        if (updateDto.getRoles() != null && !updateDto.getRoles().isEmpty()) {
            // Clear existing roles
            user.getRoles().clear();
            
            // Add new roles
            for (String roleStr : updateDto.getRoles()) {
                try {
                    RoleType roleType = RoleType.valueOf(roleStr.toUpperCase());
                    Role role = roleRepository.findByName(roleType)
                            .orElseThrow(() -> new BadRequestException("Role not found: " + roleType));
                    user.addRole(role);
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid role: " + roleStr);
                }
            }
            
            // Update legacy role field for backward compatibility
            if (!user.getRoles().isEmpty()) {
                user.setRole(user.getRoles().iterator().next());
            }
        }
        
        // Update account status if provided (admin operation)
        if (updateDto.getEnabled() != null) {
            user.setEnabled(updateDto.getEnabled());
        }
        
        if (updateDto.getAccountNonLocked() != null) {
            user.setAccountNonLocked(updateDto.getAccountNonLocked());
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
        
        // Add to roles set (new system)
        user.addRole(role);
        // Also update legacy field for backward compatibility
        user.setRole(role);
        
        User updatedUser = userRepository.save(user);
        
        log.info("Role assigned successfully to user {}", userId);
        return updatedUser;
    }
    
    @Override
    @Transactional
    public User assignRoles(Long userId, java.util.Set<RoleType> roleTypes) {
        log.info("Assigning roles {} to user {}", roleTypes, userId);
        
        User user = getUserById(userId);
        
        // Clear existing roles and add new ones
        user.getRoles().clear();
        
        for (RoleType roleType : roleTypes) {
            Role role = roleRepository.findByName(roleType)
                    .orElseThrow(() -> new BadRequestException("Role not found: " + roleType));
            user.addRole(role);
        }
        
        // Update legacy field for backward compatibility
        if (!user.getRoles().isEmpty()) {
            user.setRole(user.getRoles().iterator().next());
        }
        
        User updatedUser = userRepository.save(user);
        
        log.info("Roles assigned successfully to user {}", userId);
        return updatedUser;
    }
    
    @Override
    @Transactional
    public User removeRole(Long userId, RoleType roleType) {
        log.info("Removing role {} from user {}", roleType, userId);
        
        User user = getUserById(userId);
        
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new BadRequestException("Role not found: " + roleType));
        
        user.removeRole(role);
        
        // Ensure user has at least one role
        if (user.getRoles().isEmpty()) {
            throw new BadRequestException("User must have at least one role");
        }
        
        // Update legacy field
        user.setRole(user.getRoles().iterator().next());
        
        User updatedUser = userRepository.save(user);
        
        log.info("Role removed successfully from user {}", userId);
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
