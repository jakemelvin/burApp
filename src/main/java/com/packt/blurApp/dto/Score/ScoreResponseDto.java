package com.packt.blurApp.dto.Score;

import com.packt.blurApp.dto.User.UserResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreResponseDto {
  private Long id;
  private int value;
  private int rank;
  private UserResponseDto user;
}
