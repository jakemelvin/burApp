package com.packt.blurApp.service.race;

import java.util.List;

import org.springframework.stereotype.Service;
import com.packt.blurApp.dto.RaceParameters.AddRaceParametersDto;
import com.packt.blurApp.dto.User.RacePlayersDto;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.Party;
import com.packt.blurApp.model.Race;
import com.packt.blurApp.model.RaceParameters;
import com.packt.blurApp.model.User;
import com.packt.blurApp.repository.RaceRepository;
import com.packt.blurApp.repository.UserRepository;
import com.packt.blurApp.service.party.IPartyService;
import com.packt.blurApp.service.raceParameters.IRaceParametersService;
import com.packt.blurApp.service.user.IUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RaceService implements IRaceService {
  private final RaceRepository raceRepository;
  private final IPartyService partyService;
  private final IRaceParametersService raceParametersService;
  private final IUserService userService;
  private final UserRepository userRepository;

  @Override
  public Race getRaceById(Long id) {
    return raceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExceptions("Race not found!"));
  }

  @Override
  public Race createRace(Long partyId, List<AddRaceParametersDto> raceParametersDtos) {
    Race createdRace = new Race();
    Party party = partyService.getPartyById(partyId);
    createdRace.setParty(party);
    raceParametersDtos.forEach(parameter -> {
      RaceParameters raceParam = raceParametersService.getRaceParameterById(parameter.getId());
      if (raceParam != null) {
        createdRace.addRaceParameter(raceParam);
      }
    });
    return raceRepository.save(createdRace);
  }

  @Override
  public Race updateRacePlayers(List<RacePlayersDto> racePlayers, Long raceId) {
    try {
      Race raceToUpdate = getRaceById(raceId);
      racePlayers.forEach(player -> {
        User racers = userService.getUserById(player.getId());
        if (racers != null) {
          raceToUpdate.addRacers(racers);
          userRepository.save(racers);
        }
      });
      return raceRepository.save(raceToUpdate);
    } catch (ResourceNotFoundExceptions e) {
      throw new ResourceNotFoundExceptions(e.getMessage());
    }
  }

}
