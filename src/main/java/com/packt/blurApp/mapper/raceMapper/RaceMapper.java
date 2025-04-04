package com.packt.blurApp.mapper.raceMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.packt.blurApp.dto.Race.RaceResponseDto;
import com.packt.blurApp.mapper.partyMapper.PartyMapper;
import com.packt.blurApp.mapper.scoreMapper.ScoreMapper;
import com.packt.blurApp.mapper.userMapper.UserResponseMapper;
import com.packt.blurApp.model.Race;

public class RaceMapper {
  public static RaceResponseDto toRaceResponseDto(Race race) {
    RaceResponseDto dto = new RaceResponseDto();
    dto.setId(race.getId());
    dto.setParty(PartyMapper.toPartyResponseDto(race.getParty()));
    dto.setRaceParameters(race.getRaceParameters());
    dto.setCreatedAt(race.getCreatedAt());
    race.getRacers().forEach(racer -> {
      dto.getRacers().add(UserResponseMapper.toUserResponseDto(racer));
    });
    ;
    race.getScores().forEach(score -> {
      dto.getScores().add(ScoreMapper.toScoreResponseDto(score));
    });
    return dto;
  }

  public static Set<RaceResponseDto> toRaceResponseDtoList(List<Race> races) {
    Set<RaceResponseDto> dtoList = new HashSet<>();
    races.forEach(race -> {
      dtoList.add(toRaceResponseDto(race));
    });
    return dtoList;
  }
}
