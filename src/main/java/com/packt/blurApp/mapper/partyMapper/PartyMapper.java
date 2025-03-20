package com.packt.blurApp.mapper.partyMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.packt.blurApp.dto.Party.PartyGetResponseDto;
import com.packt.blurApp.dto.Party.PartyResponseDto;
import com.packt.blurApp.mapper.raceMapper.RaceMapper;
import com.packt.blurApp.model.Party;

public class PartyMapper {
  public static PartyResponseDto toPartyResponseDto(Party party) {
    PartyResponseDto dto = new PartyResponseDto();
    dto.setId(party.getId());
    dto.setDatePlayed(party.getDatePlayed());
    return dto;
  }

  public static PartyGetResponseDto toPartyGetResponseDto(Party party) {
    PartyGetResponseDto dto = new PartyGetResponseDto();
    dto.setDatePlayed(party.getDatePlayed());
    dto.setId(party.getId());
    party.getRacesPlayed().forEach(race -> {
      dto.getRacesPlayed().add(RaceMapper.toRaceResponseDto(race));
    });
    return dto;
  }

  public static Set<PartyGetResponseDto> toPartyGetResponseDtoList(List<Party> parties) {
    Set<PartyGetResponseDto> dtoList = new HashSet<>();
    parties.forEach(race -> {
      dtoList.add(toPartyGetResponseDto(race));
    });
    return dtoList;
  }
}