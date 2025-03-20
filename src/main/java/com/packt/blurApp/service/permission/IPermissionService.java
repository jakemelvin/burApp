package com.packt.blurApp.service.permission;

import java.util.List;

import com.packt.blurApp.dto.Permission.AddPermissionDto;
import com.packt.blurApp.model.Permission;

public interface IPermissionService {
  Permission createPermission(AddPermissionDto addPermissionDto);

  void deletePermissionById(Long id);

  List<Permission> getAllPermissions();
}
