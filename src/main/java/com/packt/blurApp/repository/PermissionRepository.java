package com.packt.blurApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packt.blurApp.model.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

}
