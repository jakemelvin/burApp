package com.packt.blurApp.service.user;

import java.util.List;

import com.packt.blurApp.dto.User.AddUserDto;
import com.packt.blurApp.model.User;

public interface IUserService {
  User getUserById(Long userId);

  User createUser(AddUserDto addUserDto);

  void deleteUserById(Long userId);

  List<User> getAllUsers();
}
