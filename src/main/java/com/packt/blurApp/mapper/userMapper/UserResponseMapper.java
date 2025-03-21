package com.packt.blurApp.mapper.userMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.packt.blurApp.dto.User.UserGlobalResponseDto;
import com.packt.blurApp.dto.User.UserResponseDto;
import com.packt.blurApp.dto.User.UserSignInResponseDto;
import com.packt.blurApp.mapper.raceMapper.RaceMapper;
import com.packt.blurApp.model.User;

public class UserResponseMapper {
  public static UserResponseDto toUserResponseDto(User user) {
    UserResponseDto dto = new UserResponseDto();
    dto.setId(user.getId());
    dto.setUserName(user.getUserName());
    return dto;
  }

  public static UserSignInResponseDto toUserSignInResponseDto(User user) {
    UserSignInResponseDto dto = new UserSignInResponseDto();
    dto.setId(user.getId());
    dto.setUserName(user.getUserName());
    user.getPermissions().forEach(permission -> {
      dto.getPermissions().add(permission);
    });
    return dto;
  }

  public static UserGlobalResponseDto toUserGlobalResponseDto(User user) {
    UserGlobalResponseDto dto = new UserGlobalResponseDto();
    dto.setId(user.getId());
    dto.setUserName(user.getUserName());
    user.getRaces().forEach(race -> {
      dto.getRaces().add(RaceMapper.toRaceResponseDto(race));
    });
    user.getPermissions().forEach(permission -> {
      dto.getPermissions().add(permission);
    });
    return dto;
  }

  public static Set<UserGlobalResponseDto> toUserGlobalResponseDtoList(List<User> users) {
    Set<UserGlobalResponseDto> dtoList = new HashSet<>();
    users.forEach(user -> {
      dtoList.add(toUserGlobalResponseDto(user));
    });
    return dtoList;
  }
}
