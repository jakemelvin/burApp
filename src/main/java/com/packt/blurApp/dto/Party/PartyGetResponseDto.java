package com.packt.blurApp.dto.Party;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartyGetResponseDto {
  private Long id;
  private LocalDateTime datePlayed;

  private PartyUserMiniDto creator;
  private java.util.Set<PartyUserMiniDto> managers = new java.util.HashSet<>();

  // NOTE: races are fetched via /races/party/{partyId} to keep party payload lightweight
  // private Set<RaceResponseDto> racesPlayed = new HashSet<>();
}
