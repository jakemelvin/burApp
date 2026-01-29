package com.packt.blurApp.service.role;

import com.packt.blurApp.exceptions.BadRequestException;
import com.packt.blurApp.exceptions.ConflictException;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.Role;
import com.packt.blurApp.model.enums.PermissionType;
import com.packt.blurApp.config.security.RoleNames;
import com.packt.blurApp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional
    public Role createRole(String name, String description, Set<PermissionType> permissions) {
        String normalized = name != null ? name.trim().toUpperCase() : null;
        if (normalized == null || normalized.isBlank()) {
            throw new BadRequestException("Role name is required");
        }
        if (RoleNames.GREAT_ADMIN.equalsIgnoreCase(normalized)) {
            throw new BadRequestException("Cannot create GREAT_ADMIN role");
        }
        if (roleRepository.existsByName(normalized)) {
            throw new ConflictException("Role already exists: " + normalized);
        }
        Role role = Role.builder()
                .name(normalized)
                .description(description)
                .permissions(permissions)
                .build();
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role updateRole(Long id, String description, Set<PermissionType> permissions) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptions("Role not found with ID: " + id));
        if (RoleNames.GREAT_ADMIN.equalsIgnoreCase(role.getName())) {
            throw new BadRequestException("Cannot modify GREAT_ADMIN role");
        }
        if (description != null) role.setDescription(description);
        if (permissions != null) role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptions("Role not found with ID: " + id));
        // Protect critical roles
        if (RoleNames.GREAT_ADMIN.equalsIgnoreCase(role.getName())) {
            throw new BadRequestException("Cannot delete GREAT_ADMIN role");
        }
        roleRepository.delete(role);
    }
}
