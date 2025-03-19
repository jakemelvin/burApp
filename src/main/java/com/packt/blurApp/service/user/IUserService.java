package com.packt.blurApp.service.user;

import com.packt.blurApp.model.User;

public interface IUserService {
  User getUserById(Long userId);
}
