package com.packt.blurApp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.packt.blurApp.dto.Permission.AddPermissionDto;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.permission.IPermissionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/permissions")
public class PermissionController {
  private final IPermissionService permissionService;

  @GetMapping
  public ResponseEntity<ApiResponse> getAllPermissions() {
    return ResponseEntity
        .ok(new ApiResponse("Permissions fetched successfully", permissionService.getAllPermissions()));
  }

  @DeleteMapping("/delete")
  public ResponseEntity<ApiResponse> deletePermissionById(@RequestParam Long permissionId) {
    try {
      permissionService.deletePermissionById(permissionId);
      return ResponseEntity.ok(new ApiResponse("Permission deleted successfully", null));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @PostMapping("/create")
  public ResponseEntity<ApiResponse> createPermission(@RequestBody AddPermissionDto addPermissionDto) {
    return ResponseEntity
        .ok(new ApiResponse("Permission created successfully", permissionService.createPermission(addPermissionDto)));
  }
}
