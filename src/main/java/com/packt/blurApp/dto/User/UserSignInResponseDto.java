package com.packt.blurApp.dto.User;

import java.util.HashSet;
import java.util.Set;

import com.packt.blurApp.model.Permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSignInResponseDto {
  private Long id;
  private String userName;
  private Set<Permission> permissions = new HashSet<>();
}
