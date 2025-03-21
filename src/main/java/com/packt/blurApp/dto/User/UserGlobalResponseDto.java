package com.packt.blurApp.dto.User;

import java.util.HashSet;
import java.util.Set;

import com.packt.blurApp.dto.Race.RaceResponseDto;
import com.packt.blurApp.model.Permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGlobalResponseDto {
  private Long id;
  private String userName;
  private Set<Permission> permissions = new HashSet<>();
  private Set<RaceResponseDto> races  = new HashSet<>();
}
