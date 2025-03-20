package com.packt.blurApp.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.packt.blurApp.dto.User.AddUserDto;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.user.IUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
  private final IUserService userService;

  @GetMapping
  public ResponseEntity<ApiResponse> getAllUsers() {
    return ResponseEntity.ok(new ApiResponse("Users fetched successfully", userService.getAllUsers()));
  }

  @PostMapping("/create")
  public ResponseEntity<ApiResponse> createUser(@RequestBody AddUserDto userDtos) {
    return ResponseEntity.ok(new ApiResponse("User created successfully", userService.createUser(userDtos)));
  }

  @GetMapping("/get-by-id")
  public ResponseEntity<ApiResponse> getUserById(@RequestParam Long userId) {
    try {
      return ResponseEntity.ok(new ApiResponse("User fetched successfully", userService.getUserById(userId)));

    } catch (ResourceNotFoundExceptions e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @DeleteMapping("/delete")
  public ResponseEntity<ApiResponse> deleteUser(@RequestParam Long userId) {
    try {
      userService.deleteUserById(userId);
      return ResponseEntity.ok(new ApiResponse("User deleted successfully", null));

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }
}
