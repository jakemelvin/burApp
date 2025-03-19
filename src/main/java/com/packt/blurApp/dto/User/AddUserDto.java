package com.packt.blurApp.dto.User;

import java.util.HashSet;
import java.util.Set;

import com.packt.blurApp.model.Permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddUserDto {
  private String userName;
  private String password;
  private Set<Permission> permissions = new HashSet<>();
}
