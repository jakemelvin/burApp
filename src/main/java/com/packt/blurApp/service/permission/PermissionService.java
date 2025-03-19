package com.packt.blurApp.service.permission;

import java.util.List;

import org.springframework.stereotype.Service;

import com.packt.blurApp.dto.Permission.AddPermissionDto;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.Permission;
import com.packt.blurApp.repository.PermissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService implements IPermissionService {
  private final PermissionRepository permissionRepository;

  @Override
  public Permission createPermission(AddPermissionDto addPermissionDto) {
    Permission newPermission = new Permission();
    newPermission.setIsActive(false);
    newPermission.setName(addPermissionDto.getName());
    return permissionRepository.save(newPermission);
  }

  @Override
  public void deletePermissionById(Long id) {
    try {
      permissionRepository.deleteById(id);
    } catch (ResourceNotFoundExceptions e) {
      throw new ResourceNotFoundExceptions("Permission Not found!");
    }
  }

  @Override
  public List<Permission> getAllPermissions() {
    return permissionRepository.findAll();
  };

}
