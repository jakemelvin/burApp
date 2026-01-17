package com.packt.blurApp.service.user;

import java.util.List;

import com.packt.blurApp.dto.User.AddUserDto;
import com.packt.blurApp.dto.User.UserUpdateDto;
import com.packt.blurApp.model.User;
import com.packt.blurApp.model.enums.RoleType;

public interface IUserService {
    User getUserById(Long userId);
    
    User createUser(AddUserDto addUserDto);
    
    User updateUser(Long userId, UserUpdateDto updateDto);
    
    User updateUserProfile(Long userId, UserUpdateDto updateDto);
    
    User assignRole(Long userId, RoleType roleType);
    
    void deleteUserById(Long userId);
    
    List<User> getAllUsers();
    
    User getCurrentUser();
}
