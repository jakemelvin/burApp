package com.packt.blurApp.service.race;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packt.blurApp.exceptions.BadRequestException;
import com.packt.blurApp.exceptions.ForbiddenException;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.*;
import com.packt.blurApp.model.enums.AttributionType;
import com.packt.blurApp.model.enums.RaceStatus;
import com.packt.blurApp.repository.*;
import com.packt.blurApp.service.party.IPartyService;
import com.packt.blurApp.service.user.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaceService implements IRaceService {
    private final RaceRepository raceRepository;
    private final IPartyService partyService;
    private final IUserService userService;
    private final UserRepository userRepository;
    private final RaceParametersRepository raceParametersRepository;
    private final AttributionRepository attributionRepository;
    private final CarRepository carRepository;
    private final CardRepository cardRepository;
    private final Random random = new Random();

    @Override
    public Race getRaceById(Long id) {
        log.debug("Fetching race by ID: {}", id);
        return raceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptions("Race not found with ID: " + id));
    }

    @Override
    @Transactional
    public Race createRace(Long partyId, AttributionType attributionType) {
        log.info("Creating race for party {} with attribution type {}", partyId, attributionType);
        
        User currentUser = userService.getCurrentUser();
        Party party = partyService.getPartyById(partyId);
        
        // Verify user is a member of the party
        if (!party.isMember(currentUser)) {
            throw new ForbiddenException("Only party members can create races");
        }
        
        // Select random card (map)
        List<Card> allCards = cardRepository.findAll();
        if (allCards.isEmpty()) {
            throw new BadRequestException("No cards available. Please add cards to the system.");
        }
        Card randomCard = allCards.get(random.nextInt(allCards.size()));
        
        // Create race
        Race race = Race.builder()
                .party(party)
                .creator(currentUser)
                .card(randomCard)
                .attributionType(attributionType)
                .status(RaceStatus.PENDING)
                .build();
        
        // Add race parameters (setup automatically)
        List<RaceParameters> allParameters = raceParametersRepository.findAll();
        allParameters.forEach(parameter -> {
            // Randomly activate parameters
            if (random.nextBoolean()) {
                race.addRaceParameter(parameter);
            }
        });
        
        // Persist race (party relationship is already set via builder)
        Race savedRace = raceRepository.save(race);

        log.info("Race created successfully with ID: {}", savedRace.getId());
        return savedRace;
    }

    @Override
    @Transactional
    public Race addParticipant(Long raceId, Long userId) {
        log.info("Adding participant {} to race {}", userId, raceId);
        
        Race race = getRaceById(raceId);
        
        if (race.getStatus() != RaceStatus.PENDING) {
            throw new BadRequestException("Can only add participants to pending races");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundExceptions("User not found with ID: " + userId));
        
        if (!race.getParty().isMember(user)) {
            throw new BadRequestException("User must be a party member to join the race");
        }
        
        if (race.isParticipant(user)) {
            throw new BadRequestException("User is already a participant in this race");
        }
        
        race.addParticipant(user);
        Race updatedRace = raceRepository.save(race);
        
        log.info("Participant {} added to race {}", userId, raceId);
        return updatedRace;
    }

    @Override
    @Transactional
    public Race removeParticipant(Long raceId, Long userId) {
        log.info("Removing participant {} from race {}", userId, raceId);
        
        Race race = getRaceById(raceId);
        
        if (race.getStatus() != RaceStatus.PENDING) {
            throw new BadRequestException("Can only remove participants from pending races");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundExceptions("User not found with ID: " + userId));
        
        if (!race.isParticipant(user)) {
            throw new BadRequestException("User is not a participant in this race");
        }
        
        race.removeParticipant(user);
        Race updatedRace = raceRepository.save(race);
        
        log.info("Participant {} removed from race {}", userId, raceId);
        return updatedRace;
    }

    @Override
    @Transactional
    public Race startRace(Long raceId) {
        log.info("Starting race {}", raceId);
        
        User currentUser = userService.getCurrentUser();
        Race race = getRaceById(raceId);
        
        // Only party managers can start races
        if (!race.getParty().isManager(currentUser)) {
            throw new ForbiddenException("Only party managers can start races");
        }
        
        if (race.getStatus() != RaceStatus.PENDING) {
            throw new BadRequestException("Race must be in PENDING status to start");
        }
        
        if (race.getParticipants().isEmpty()) {
            throw new BadRequestException("Cannot start race without participants");
        }
        
        // Create car attributions
        createAttributions(race);
        
        // Assign random score collector from participants
        List<User> participants = new ArrayList<>(race.getParticipants());
        User scoreCollector = participants.get(random.nextInt(participants.size()));
        race.setScoreCollector(scoreCollector);
        
        race.start();
        Race updatedRace = raceRepository.save(race);
        
        log.info("Race {} started successfully. Score collector: {}", raceId, scoreCollector.getUsername());
        return updatedRace;
    }

    private void createAttributions(Race race) {
        log.debug("Creating car attributions for race {}", race.getId());
        
        List<Car> allCars = carRepository.findAll();
        if (allCars.isEmpty()) {
            throw new BadRequestException("No cars available. Please add cars to the system.");
        }
        
        if (race.getAttributionType() == AttributionType.ALL_USERS) {
            // All users get the same random car
            Car randomCar = allCars.get(random.nextInt(allCars.size()));
            for (User participant : race.getParticipants()) {
                Attribution attribution = Attribution.builder()
                        .race(race)
                        .user(participant)
                        .car(randomCar)
                        .build();
                attributionRepository.save(attribution);
                race.addAttribution(attribution);
            }
            log.info("All participants assigned car: {}", randomCar.getName());
        } else {
            // Each user gets a different random car
            List<Car> shuffledCars = new ArrayList<>(allCars);
            Collections.shuffle(shuffledCars);
            
            int carIndex = 0;
            for (User participant : race.getParticipants()) {
                Car assignedCar = shuffledCars.get(carIndex % shuffledCars.size());
                Attribution attribution = Attribution.builder()
                        .race(race)
                        .user(participant)
                        .car(assignedCar)
                        .build();
                attributionRepository.save(attribution);
                race.addAttribution(attribution);
                carIndex++;
            }
            log.info("Each participant assigned individual random cars");
        }
    }

    @Override
    @Transactional
    public Race completeRace(Long raceId) {
        log.info("Completing race {}", raceId);
        
        User currentUser = userService.getCurrentUser();
        Race race = getRaceById(raceId);
        
        // Only party managers can complete races
        if (!race.getParty().isManager(currentUser)) {
            throw new ForbiddenException("Only party managers can complete races");
        }
        
        if (race.getStatus() != RaceStatus.IN_PROGRESS) {
            throw new BadRequestException("Race must be in IN_PROGRESS status to complete");
        }
        
        race.complete();
        Race updatedRace = raceRepository.save(race);
        
        log.info("Race {} completed successfully", raceId);
        return updatedRace;
    }

    @Override
    @Transactional
    public Race cancelRace(Long raceId) {
        log.info("Cancelling race {}", raceId);
        
        User currentUser = userService.getCurrentUser();
        Race race = getRaceById(raceId);
        
        // Only creator or party managers can cancel races
        if (!race.getCreator().equals(currentUser) && !race.getParty().isManager(currentUser)) {
            throw new ForbiddenException("Only race creator or party managers can cancel races");
        }
        
        if (race.getStatus() == RaceStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel completed race");
        }
        
        race.cancel();
        Race updatedRace = raceRepository.save(race);
        
        log.info("Race {} cancelled successfully", raceId);
        return updatedRace;
    }

    @Override
    public List<Race> getAllRaces() {
        log.debug("Fetching all races");
        return raceRepository.findAll();
    }

    @Override
    public List<Race> getRacesByPartyId(Long partyId) {
        log.debug("Fetching races for party {}", partyId);
        return raceRepository.findByParty_Id(partyId);
    }

    @Override
    public List<Race> getRacesByStatus(String status) {
        log.debug("Fetching races by status: {}", status);
        try {
            RaceStatus raceStatus = RaceStatus.valueOf(status.toUpperCase());
            return raceRepository.findByStatus(raceStatus);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid race status: " + status);
        }
    }

    @Override
    public long getTotalRacesCount() {
        return raceRepository.count();
    }
}
