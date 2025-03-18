package com.packt.blurApp.service.party;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import com.packt.blurApp.BlurAppApplication;
import com.packt.blurApp.dto.Party.AddPartyDto;
import com.packt.blurApp.dto.Party.PartyUpdateDto;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.Party;
import com.packt.blurApp.model.Race;
import com.packt.blurApp.repository.PartyRepository;
import com.packt.blurApp.service.race.IRaceService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartyService implements IPartyService {

  private final PartyRepository partyRepository;

  private final IRaceService raceService;

  @Override
  public Party createParty() {
    Party party = new Party();
    party.setDateToNow();
    return partyRepository.save(party);
  }

  @Override
  public Party getPartyById(Long id) {
    return partyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExceptions("Party not Found!"));
  }

  @Override
  public List<Party> getAllParties() {
    return partyRepository.findAll();
  }

  @Override
  public Party updateParty(PartyUpdateDto updatedParty, Long partyId) {
    try {
      Optional<Party> existingPartyOptional = partyRepository.findById(partyId);
      if (existingPartyOptional.isPresent()) {
        Set<Race> racesToAdd = null;
        Party existingParty = existingPartyOptional.get();
        updatedParty.getRaceIds().forEach(raceId -> {
          try {
            racesToAdd.add(raceService.getRaceById(raceId));
          } catch (ResourceNotFoundExceptions e) {
            throw new ResourceNotFoundExceptions("Race Not found!");
          }
        });

        racesToAdd.forEach(existingParty::addRace);

        return partyRepository.save(existingParty);
      } else {
        throw new ResourceNotFoundExceptions("Party Not Found");
      }
    } catch (ResourceNotFoundExceptions e) {
      throw new ResourceNotFoundExceptions("Resources not found!");
    }
  }

}
