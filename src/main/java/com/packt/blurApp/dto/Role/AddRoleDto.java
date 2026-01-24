package com.packt.blurApp.dto.Role;

import com.packt.blurApp.model.enums.PermissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class AddRoleDto {
    @NotBlank
    private String name; // RoleType enum name
    private String description;
    @NotNull
    private Set<PermissionType> permissions;
}
