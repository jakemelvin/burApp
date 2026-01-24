package com.packt.blurApp.controller;

import com.packt.blurApp.dto.Role.AddRoleDto;
import com.packt.blurApp.dto.Role.RoleResponseDto;
import com.packt.blurApp.dto.Role.UpdateRoleDto;
import com.packt.blurApp.model.Role;
import com.packt.blurApp.model.enums.PermissionType;
import com.packt.blurApp.model.enums.RoleType;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.role.IRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/roles")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ASSIGN_ROLES')")
    public ResponseEntity<ApiResponse<?>> getAll() {
        List<Role> roles = roleService.getAll();
        List<RoleResponseDto> dtos = roles.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Roles fetched successfully", dtos));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ASSIGN_ROLES')")
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody AddRoleDto dto) {
        Role role = roleService.createRole(RoleType.valueOf(dto.getName()), dto.getDescription(), dto.getPermissions());
        return ResponseEntity.ok(ApiResponse.success("Role created successfully", toDto(role)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSIGN_ROLES')")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Long id, @Valid @RequestBody UpdateRoleDto dto) {
        Role role = roleService.updateRole(id, dto.getDescription(), dto.getPermissions());
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", toDto(role)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSIGN_ROLES')")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully"));
    }

    private RoleResponseDto toDto(Role r) {
        RoleResponseDto res = new RoleResponseDto();
        res.setId(r.getId());
        res.setName(r.getName().name());
        res.setDescription(r.getDescription());
        res.setPermissions(r.getPermissions().stream().map(PermissionType::name).collect(Collectors.toSet()));
        return res;
    }
}
