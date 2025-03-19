package com.packt.blurApp.service.party;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.packt.blurApp.repository.RaceRepository;
import org.springframework.stereotype.Service;
import com.packt.blurApp.dto.Party.PartyUpdateDto;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.Party;
import com.packt.blurApp.model.Race;
import com.packt.blurApp.repository.PartyRepository;
import com.packt.blurApp.repository.RaceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartyService implements IPartyService {

  private final PartyRepository partyRepository;

  private final RaceRepository raceRepository;

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
        Party existingParty = existingPartyOptional.get();
        Set<Race> racesToAdd = new HashSet<>();

        if (updatedParty.getRaceIds() != null && !updatedParty.getRaceIds().isEmpty()) {
          updatedParty.getRaceIds().forEach(raceId -> {
            try {
              Race race = raceRepository.findById(raceId).orElseThrow(() -> new ResourceNotFoundExceptions("Race not Found!"));
              racesToAdd.add(race);
            } catch (ResourceNotFoundExceptions e) {
              throw new ResourceNotFoundExceptions("Race Not found with ID: " + raceId);
            }
          });

          racesToAdd.forEach(existingParty::addRace);
        }
        return partyRepository.save(existingParty);
      } else {
        throw new ResourceNotFoundExceptions("Party Not Found with ID: " + partyId);
      }
    } catch (ResourceNotFoundExceptions e) {
      throw e;
    } catch (Exception e) {
      throw new ResourceNotFoundExceptions("Error updating Party: " + e.getMessage());
    }

  }

}
