package com.packt.blurApp.mapper.userMapper;

import com.packt.blurApp.dto.User.UserResponseDto;
import com.packt.blurApp.model.User;

public class UserResponseMapper {
  public static UserResponseDto toUserResponseDto(User user) {
    UserResponseDto dto = new UserResponseDto();
    dto.setId(user.getId());
    dto.setUserName(user.getUserName());
    return dto;
  }
}
