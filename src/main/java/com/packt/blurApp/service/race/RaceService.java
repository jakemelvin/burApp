package com.packt.blurApp.service.race;

import java.util.List;

import org.springframework.stereotype.Service;
import com.packt.blurApp.dto.User.RacePlayersDto;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.Party;
import com.packt.blurApp.model.Race;
import com.packt.blurApp.model.RaceParameters;
import com.packt.blurApp.model.User;
import com.packt.blurApp.repository.RaceParametersRepository;
import com.packt.blurApp.repository.RaceRepository;
import com.packt.blurApp.repository.UserRepository;
import com.packt.blurApp.service.party.IPartyService;
import com.packt.blurApp.service.user.IUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RaceService implements IRaceService {
  private final RaceRepository raceRepository;
  private final IPartyService partyService;
  private final IUserService userService;
  private final UserRepository userRepository;
  private final RaceParametersRepository raceParametersRepository;

  @Override
  public Race getRaceById(Long id) {
    return raceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExceptions("Race not found!"));
  }

  @Override
  public Race createRace(Long partyId) {
    Race createdRace = new Race();
    Party party = partyService.getPartyById(partyId);
    createdRace.setParty(party);
    List<RaceParameters> raceParameters = raceParametersRepository.findAll();
    raceParameters.forEach(parameter -> {
      if (parameter != null) {
        boolean randomBoolean = Math.random() < 0.5;
        parameter.setIsActive(randomBoolean);
        createdRace.getRaceParameters().add(parameter);

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

  @Override
  public List<Race> getAllRaces() {
    return raceRepository.findAll();
  }

  @Override
  public List<Race> getRaceByPartyId(Long partyId) {
    try {
      return raceRepository.findByParty_Id(partyId);
    } catch (ResourceNotFoundExceptions e) {
      throw new ResourceNotFoundExceptions("Race not found");
    }
  }

}
