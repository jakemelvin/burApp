package com.packt.blurApp.dto.Party;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartyGetResponseDto {
  private Long id;
  private LocalDateTime datePlayed;
  private boolean active;

  // Host (creator) of the party
  private PartyUserMiniDto host;
  
  // Co-hosts who can help manage the party
  private Set<PartyUserMiniDto> coHosts = new HashSet<>();
  
  // All participants (non-managers)
  private Set<PartyUserMiniDto> participants = new HashSet<>();
  
  // All members with their roles (for detailed view)
  private List<PartyMemberDto> members = new ArrayList<>();

  // Legacy fields for backward compatibility
  private PartyUserMiniDto creator;
  private Set<PartyUserMiniDto> managers = new HashSet<>();

  // NOTE: races are fetched via /races/party/{partyId} to keep party payload lightweight
}
