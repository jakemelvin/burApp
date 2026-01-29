package com.packt.blurApp.dto.Role;

import com.packt.blurApp.model.enums.PermissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class AddRoleDto {
    @NotBlank
    private String name; // Dynamic role name (String). GREAT_ADMIN is reserved.
    private String description;
    @NotNull
    private Set<PermissionType> permissions;
}
