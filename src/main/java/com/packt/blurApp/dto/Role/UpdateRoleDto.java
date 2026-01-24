package com.packt.blurApp.dto.Role;

import com.packt.blurApp.model.enums.PermissionType;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateRoleDto {
    private String description;
    private Set<PermissionType> permissions;
}
