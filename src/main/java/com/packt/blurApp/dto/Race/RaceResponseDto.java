package com.packt.blurApp.dto.Race;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.packt.blurApp.dto.Party.PartyResponseDto;
import com.packt.blurApp.dto.Score.ScoreResponseDto;
import com.packt.blurApp.dto.User.UserResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaceResponseDto {
  private Long id;
  private PartyResponseDto party;
  private LocalDateTime createdAt;
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;
  private String status;
  private String attributionType;
  @Builder.Default
  private Set<ScoreResponseDto> scores = new HashSet<>();
  @Builder.Default
  private Set<UserResponseDto> racers = new HashSet<>();
  @Builder.Default
  private Set<RaceParameterDto> raceParameters = new HashSet<>();
  private CarDto car;
  private CardDto card;
  @Builder.Default
  private Set<AttributionDto> attributions = new HashSet<>();
  
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CarDto {
    private Long id;
    private String name;
    private String imageUrl;
  }
  
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CardDto {
    private Long id;
    private String location;
    private String track;
    private String imageUrl;
  }
  
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class RaceParameterDto {
    private Long id;
    private String name;
    private Boolean isActive;
    private String downloadUrl;
  }
  
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class AttributionDto {
    private Long id;
    private UserResponseDto user;
    private CarDto car;
    private String notes;
  }
}
