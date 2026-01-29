package com.packt.blurApp.service.role;

import com.packt.blurApp.model.Role;
import com.packt.blurApp.model.enums.PermissionType;

import java.util.List;
import java.util.Set;

public interface IRoleService {
    List<Role> getAll();
    Role createRole(String name, String description, Set<PermissionType> permissions);
    Role updateRole(Long id, String description, Set<PermissionType> permissions);
    void deleteRole(Long id);
}
