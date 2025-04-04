package com.packt.blurApp.dto.Race;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.packt.blurApp.dto.Party.PartyResponseDto;
import com.packt.blurApp.dto.Score.ScoreResponseDto;
import com.packt.blurApp.dto.User.UserResponseDto;
import com.packt.blurApp.model.RaceParameters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaceResponseDto {
  private Long id;
  private PartyResponseDto party;
  private LocalDateTime createdAt;
  private Set<ScoreResponseDto> scores = new HashSet<>();
  private Set<UserResponseDto> racers = new HashSet<>();
  private Set<RaceParameters> raceParameters = new HashSet<>();
}
