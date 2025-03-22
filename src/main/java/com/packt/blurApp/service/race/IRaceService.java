package com.packt.blurApp.service.race;

import java.util.List;

import com.packt.blurApp.dto.User.RacePlayersDto;
import com.packt.blurApp.model.Race;

public interface IRaceService {
  Race getRaceById(Long id);

  Race createRace(Long partyId);

  Race updateRacePlayers(List<RacePlayersDto> racePlayers, Long raceId);

  List<Race> getAllRaces();

  List<Race> getRaceByPartyId(Long partyId);
}
