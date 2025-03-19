package com.packt.blurApp.service.user;

import java.util.List;

import org.springframework.stereotype.Service;

import com.packt.blurApp.dto.User.AddUserDto;
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

  @Override
  public User createUser(AddUserDto addUserDto) {
    User newUser = new User();
    newUser.setPassword(addUserDto.getPassword());
    newUser.setUserName(addUserDto.getUserName());
    addUserDto.getPermissions().forEach(permission -> {
      newUser.getPermissions().add(permission);
    });
    return userRepository.save(newUser);
  }

  @Override
  public void deleteUserById(Long userId) {
    try {
      userRepository.deleteById(userId);
    } catch (ResourceNotFoundExceptions e) {
      throw new ResourceNotFoundExceptions("User not found");
    }
  }

  @Override
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }
}
