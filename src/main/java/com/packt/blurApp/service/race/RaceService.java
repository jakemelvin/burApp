package com.packt.blurApp.service.race;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packt.blurApp.exceptions.BadRequestException;
import com.packt.blurApp.exceptions.ForbiddenException;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.config.security.RoleNames;
import com.packt.blurApp.model.*;
import com.packt.blurApp.model.enums.AttributionType;
import com.packt.blurApp.model.enums.PartyRole;
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
    private final PartyRepository partyRepository;

    @Override
    @Transactional(readOnly = true)
    public Race getRaceById(Long id) {
        log.debug("Fetching race by ID: {}", id);
        return raceRepository.findWithGraphById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptions("Race not found with ID: " + id));
    }

    @Override
    @Transactional
    public Race createRace(Long partyId, AttributionType attributionType) {
        log.info("Creating race for party {} with attribution type {}", partyId, attributionType);
        
        User currentUser = userService.getCurrentUser();
        Party party = partyService.getPartyById(partyId);
        
        // New rule: users do NOT explicitly join parties.
        // If a user creates a race for the daily party, they are implicitly considered a party member.
        if (!party.isMember(currentUser)) {
            PartyMember partyMember = PartyMember.builder()
                    .party(party)
                    .user(currentUser)
                    .role(PartyRole.PARTICIPANT)
                    .build();
            party.addPartyMember(partyMember);
            partyRepository.save(party);
        }
        
        // Select random card (map)
        List<Card> allCards = cardRepository.findAll();
        if (allCards.isEmpty()) {
            throw new BadRequestException("No cards available. Please add cards to the system.");
        }

        // Avoid repeating the same card twice in a row for the same party (best effort).
        Long lastCardId = raceRepository.findTopByParty_IdOrderByIdDesc(partyId)
                .map(Race::getCard)
                .map(Card::getId)
                .orElse(null);

        List<Card> selectableCards = allCards;
        if (lastCardId != null && allCards.size() > 1) {
            selectableCards = allCards.stream().filter(c -> !lastCardId.equals(c.getId())).toList();
            if (selectableCards.isEmpty()) {
                selectableCards = allCards;
            }
        }

        Card randomCard = selectableCards.get(ThreadLocalRandom.current().nextInt(selectableCards.size()));
        
        // Create race
        Race race = Race.builder()
                .party(party)
                .creator(currentUser)
                .card(randomCard)
                .attributionType(attributionType)
                .status(RaceStatus.PENDING)
                .build();
        
        // Add race parameters (setup automatically)
        // Randomly select between 0 and ALL available parameters with equal probability
        List<RaceParameters> allParameters = raceParametersRepository.findAll();
        if (!allParameters.isEmpty()) {
            // Shuffle the parameters list to randomize selection order
            List<RaceParameters> shuffledParameters = new ArrayList<>(allParameters);
            Collections.shuffle(shuffledParameters);
            
            // Randomly determine how many parameters to select (0 to allParameters.size(), inclusive)
            // Each count has equal probability
            int parameterCount = ThreadLocalRandom.current().nextInt(allParameters.size() + 1);
            
            // Add the selected number of parameters
            for (int i = 0; i < parameterCount; i++) {
                race.addRaceParameter(shuffledParameters.get(i));
            }
            
            log.debug("Selected {} race parameters out of {} available", parameterCount, allParameters.size());
        }
        
        // Persist race (party relationship is already set via builder)
        // Party membership changes (party.addMember) are persisted by JPA at transaction commit.
        Race savedRace = raceRepository.save(race);

        // Re-fetch with entity graph so controller->mapper never triggers LazyInitializationException
        Race hydratedRace = getRaceById(savedRace.getId());

        log.info("Race created successfully with ID: {}", hydratedRace.getId());
        return hydratedRace;
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
        
        // GREAT_ADMIN cannot participate in races - they are administrators only
        if (user.getRole() != null && RoleNames.GREAT_ADMIN.equals(user.getRole().getName())) {
            throw new ForbiddenException("Administrators cannot participate in races. Only racers can join.");
        }
        
        // New rule: racers join races (not parties). Party membership is implied.
        // Ensure the user is a party member (so existing backend invariants still hold).
        Party party = race.getParty();
        if (party != null && !party.isMember(user)) {
            PartyMember partyMember = PartyMember.builder()
                    .party(party)
                    .user(user)
                    .role(PartyRole.PARTICIPANT)
                    .build();
            party.addPartyMember(partyMember);
            partyRepository.save(party);
        }
        
        if (race.isParticipant(user)) {
            throw new BadRequestException("User is already a participant in this race");
        }
        
        // Party membership changes (party.addMember) are persisted by JPA at transaction commit.
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
        
        // New rule: racers join a specific race (not the party).
        // Any joined racer can start the race.
        if (!race.isParticipant(currentUser)) {
            throw new ForbiddenException("Only joined race participants can start the race");
        }
        
        if (race.getStatus() != RaceStatus.PENDING) {
            throw new BadRequestException("Race must be in PENDING status to start");
        }
        
        if (race.getParticipants().isEmpty()) {
            throw new BadRequestException("Cannot start race without participants");
        }
        
        // Create car attributions only if they don't exist
        if (race.getAttributions() == null || race.getAttributions().isEmpty()) {
            log.info("No attributions found for race {}. Creating attributions automatically.", raceId);
            createAttributions(race);
        } else {
            log.info("Race {} already has {} attributions. Skipping attribution creation.", 
                    raceId, race.getAttributions().size());
        }
        
        // Assign random score collector from participants
        List<User> participants = new ArrayList<>(race.getParticipants());
        User scoreCollector = participants.get(ThreadLocalRandom.current().nextInt(participants.size()));
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
            Car randomCar = allCars.get(ThreadLocalRandom.current().nextInt(allCars.size()));
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
        
        // Any joined race participant can complete the race (same rule as starting)
        if (!race.isParticipant(currentUser)) {
            throw new ForbiddenException("Only joined race participants can complete the race");
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
        if (!race.getCreator().equals(currentUser) && !race.getParty().canManage(currentUser)) {
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
    @Transactional
    public Race changeCard(Long raceId) {
        log.info("Changing card for race {}", raceId);
        
        Race race = getRaceById(raceId);
        
        if (race.getStatus() != RaceStatus.PENDING) {
            throw new BadRequestException("Can only change card for pending races");
        }
        
        // Get all available cards
        List<Card> allCards = cardRepository.findAll();
        if (allCards.isEmpty()) {
            throw new BadRequestException("No cards available in the system");
        }
        
        // Avoid selecting the same card
        Card currentCard = race.getCard();
        List<Card> selectableCards = allCards;
        if (currentCard != null && allCards.size() > 1) {
            selectableCards = allCards.stream()
                    .filter(c -> !c.getId().equals(currentCard.getId()))
                    .toList();
            if (selectableCards.isEmpty()) {
                selectableCards = allCards;
            }
        }
        
        // Select random card
        Card newCard = selectableCards.get(ThreadLocalRandom.current().nextInt(selectableCards.size()));
        race.setCard(newCard);
        
        Race updatedRace = raceRepository.save(race);
        log.info("Card changed for race {} to card {}", raceId, newCard.getId());
        
        return getRaceById(updatedRace.getId());
    }

    @Override
    @Transactional
    public Race assignCars(Long raceId) {
        log.info("Assigning cars for race {}", raceId);
        
        Race race = getRaceById(raceId);
        
        if (race.getStatus() != RaceStatus.PENDING) {
            throw new BadRequestException("Can only assign cars for pending races");
        }
        
        // Explicitly initialize participants to ensure they're loaded
        org.hibernate.Hibernate.initialize(race.getParticipants());
        log.debug("Race {} has {} participants", raceId, race.getParticipants().size());
        
        List<Car> allCars = carRepository.findAll();
        if (allCars.isEmpty()) {
            throw new BadRequestException("No cars available in the system");
        }
        
        AttributionType attributionType = race.getAttributionType();
        if (attributionType == null) {
            attributionType = AttributionType.PER_USER;
        }
        
        if (attributionType == AttributionType.ALL_USERS) {
            // Assign a single car to the race (global attribution) via Attribution
            // Get current car from attributions (if any) before clearing
            final Car currentCar;
            if (race.getAttributions() != null && !race.getAttributions().isEmpty()) {
                currentCar = race.getAttributions().stream()
                        .filter(a -> a.getUser() == null && a.getCar() != null)
                        .map(Attribution::getCar)
                        .findFirst()
                        .orElse(null);
                
                // Delete existing attributions explicitly
                log.info("Clearing {} existing attributions for race {}", race.getAttributions().size(), raceId);
                attributionRepository.deleteAll(race.getAttributions());
                race.getAttributions().clear();
            } else {
                currentCar = null;
            }
            
            // Ensure attributions collection is initialized
            if (race.getAttributions() == null) {
                race.setAttributions(new HashSet<>());
            }
            
            List<Car> selectableCars = allCars;
            if (currentCar != null && allCars.size() > 1) {
                selectableCars = allCars.stream()
                        .filter(c -> !c.getId().equals(currentCar.getId()))
                        .toList();
                if (selectableCars.isEmpty()) {
                    selectableCars = allCars;
                }
            }
            Car newCar = selectableCars.get(ThreadLocalRandom.current().nextInt(selectableCars.size()));
            
            // Create global attribution (no user, just car for the race)
            Attribution globalAttribution = Attribution.builder()
                    .race(race)
                    .user(null)
                    .car(newCar)
                    .notes("Global car for all participants")
                    .build();
            Attribution savedAttribution = attributionRepository.save(globalAttribution);
            race.getAttributions().add(savedAttribution);
            log.info("Assigned global car {} to race {}", newCar.getId(), raceId);
        } else {
            // PER_USER: Assign individual cars to each participant
            Set<User> participants = race.getParticipants();
            if (participants == null || participants.isEmpty()) {
                throw new BadRequestException("No participants in the race to assign cars to");
            }
            
            log.info("Assigning cars to {} participants in race {}", participants.size(), raceId);
            
            // Delete existing attributions explicitly
            if (race.getAttributions() != null && !race.getAttributions().isEmpty()) {
                log.info("Clearing {} existing attributions for race {}", race.getAttributions().size(), raceId);
                attributionRepository.deleteAll(race.getAttributions());
                race.getAttributions().clear();
            }
            
            // Ensure attributions collection is initialized
            if (race.getAttributions() == null) {
                race.setAttributions(new HashSet<>());
            }
            
            // Assign random car to each participant
            for (User participant : participants) {
                Car randomCar = allCars.get(ThreadLocalRandom.current().nextInt(allCars.size()));
                Attribution attribution = Attribution.builder()
                        .race(race)
                        .user(participant)
                        .car(randomCar)
                        .build();
                // Save each attribution immediately
                Attribution savedAttribution = attributionRepository.save(attribution);
                race.getAttributions().add(savedAttribution);
                log.info("Assigned car {} (id={}) to user {} (id={}) in race {}", 
                        randomCar.getName(), randomCar.getId(), 
                        participant.getUsername(), participant.getId(), raceId);
            }
            log.info("Total attributions created: {}", race.getAttributions().size());
        }
        
        Race updatedRace = raceRepository.save(race);
        return getRaceById(updatedRace.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Race> getAllRaces() {
        log.debug("Fetching all races");
        return raceRepository.findAll(); // overridden with entity graph
    }

    @Override
    @Transactional(readOnly = true)
    public List<Race> getRacesByPartyId(Long partyId) {
        log.debug("Fetching races for party {}", partyId);
        return raceRepository.findByParty_Id(partyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Race> getRacesByStatus(String status) {
        log.debug("Fetching races by status: {}", status);
        try {
            RaceStatus raceStatus = RaceStatus.valueOf(status.toUpperCase());
            return raceRepository.findByStatus(raceStatus); // entity graph
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid race status: " + status);
        }
    }

    @Override
    public long getTotalRacesCount() {
        return raceRepository.count();
    }
}
