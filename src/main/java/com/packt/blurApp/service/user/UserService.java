package com.packt.blurApp.service.user;

import java.util.List;

import org.springframework.stereotype.Service;

import com.packt.blurApp.dto.User.AddUserDto;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.User;
import com.packt.blurApp.repository.PermissionRepository;
import com.packt.blurApp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
  private final UserRepository userRepository;
  private final PermissionRepository permissionRepository;

  @Override
  public User getUserById(Long userId) {
    return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundExceptions("User Not found!"));
  }

  @Override
  public User createUser(AddUserDto addUserDto) {
    String userName = addUserDto.getUserName();
    String password = addUserDto.getPassword();
    User newUser = new User(userName, password);
    addUserDto.getPermissionsIds().forEach(permission -> {
      newUser.getPermissions().add(permissionRepository.findById(permission)
          .orElseThrow(() -> new ResourceNotFoundExceptions("Permission not found!")));
    });
    return userRepository.save(newUser);
  }

  @Override
  public void deleteUserById(Long userId) {
    User userToDelete = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundExceptions("User Not found!"));
    userRepository.delete(userToDelete);
  }

  @Override
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }
}
