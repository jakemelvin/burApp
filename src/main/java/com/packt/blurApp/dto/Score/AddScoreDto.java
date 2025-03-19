package com.packt.blurApp.dto.Score;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddScoreDto {
  private int value;
  private Long raceId;
  private Long userId;
}
