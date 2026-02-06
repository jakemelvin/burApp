package com.packt.blurApp.service.race;

import java.util.List;

import com.packt.blurApp.model.Race;
import com.packt.blurApp.model.enums.AttributionType;

public interface IRaceService {
    Race getRaceById(Long id);
    
    Race createRace(Long partyId, AttributionType attributionType);
    
    Race addParticipant(Long raceId, Long userId);
    
    Race removeParticipant(Long raceId, Long userId);
    
    Race startRace(Long raceId);
    
    Race completeRace(Long raceId);
    
    Race cancelRace(Long raceId);
    
    Race changeCard(Long raceId);
    
    Race assignCars(Long raceId);
    
    List<Race> getAllRaces();
    
    List<Race> getRacesByPartyId(Long partyId);
    
    List<Race> getRacesByStatus(String status);

    long getTotalRacesCount();
}
