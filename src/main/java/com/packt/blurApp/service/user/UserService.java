package com.packt.blurApp.service.user;

import org.springframework.stereotype.Service;

import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.User;
import com.packt.blurApp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
  private final UserRepository userRepository;

  @Override
  public User getUserById(Long userId) {
    return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundExceptions("User Not found!"));
  }
}
