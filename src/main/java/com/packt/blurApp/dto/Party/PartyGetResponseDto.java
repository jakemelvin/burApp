package com.packt.blurApp.dto.Party;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.packt.blurApp.dto.Race.RaceResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartyGetResponseDto {
  private Long id;
  private LocalDateTime datePlayed;
  private Set<RaceResponseDto> racesPlayed = new HashSet<>();
}
