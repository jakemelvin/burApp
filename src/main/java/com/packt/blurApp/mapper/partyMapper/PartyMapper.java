package com.packt.blurApp.mapper.partyMapper;

import com.packt.blurApp.dto.Party.PartyResponseDto;
import com.packt.blurApp.model.Party;

public class PartyMapper {
  public static PartyResponseDto toPartyResponseDto(Party party) {
    PartyResponseDto dto = new PartyResponseDto();
    dto.setId(party.getId());
    dto.setDatePlayed(party.getDatePlayed());
    return dto;
  }
}