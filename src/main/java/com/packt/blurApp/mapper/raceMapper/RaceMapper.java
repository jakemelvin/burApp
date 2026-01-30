package com.packt.blurApp.mapper.raceMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.packt.blurApp.dto.Race.RaceResponseDto;
import com.packt.blurApp.mapper.partyMapper.PartyMapper;
import com.packt.blurApp.mapper.scoreMapper.ScoreMapper;
import com.packt.blurApp.dto.User.UserMiniDto;
import com.packt.blurApp.mapper.userMapper.UserResponseMapper;
import com.packt.blurApp.model.Race;
import com.packt.blurApp.model.Attribution;
import com.packt.blurApp.model.Car;
import com.packt.blurApp.model.Card;
import com.packt.blurApp.model.RaceParameters;

public class RaceMapper {
    
    public static RaceResponseDto toRaceResponseDto(Race race) {
        if (race == null) {
            return null;
        }
        
        RaceResponseDto dto = new RaceResponseDto();
        dto.setId(race.getId());
        dto.setCreatedAt(race.getCreatedAt());
        dto.setStartedAt(race.getStartedAt());
        dto.setCompletedAt(race.getCompletedAt());
        dto.setStatus(race.getStatus() != null ? race.getStatus().name() : null);
        dto.setAttributionType(race.getAttributionType() != null ? race.getAttributionType().name() : null);
        
        // Map party
        if (race.getParty() != null) {
            dto.setParty(PartyMapper.toPartyResponseDto(race.getParty()));
        }
        
        // Map card
        if (race.getCard() != null) {
            dto.setCard(toCardDto(race.getCard()));
        }

        // Map global car (backend stores car attribution in Attributions, not directly on Race)
        // If ALL_USERS, all attributions share same car; expose it via dto.car for frontend convenience.
        if (race.getAttributionType() != null
                && race.getAttributionType().name().equals("ALL_USERS")
                && race.getAttributions() != null
                && !race.getAttributions().isEmpty()) {
            Car car = race.getAttributions().stream()
                    .map(Attribution::getCar)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            dto.setCar(toCarDto(car));
        }
        
        // Map race parameters
        if (race.getRaceParameters() != null) {
            dto.setRaceParameters(race.getRaceParameters().stream()
                .map(RaceMapper::toRaceParameterDto)
                .collect(Collectors.toSet()));
        }
        
        // Map participants (use lightweight DTO to avoid recursive mapping)
        if (race.getParticipants() != null) {
            dto.setRacers(race.getParticipants().stream()
                .filter(java.util.Objects::nonNull)
                .map(u -> new UserMiniDto(u.getId(), u.getUsername()))
                .collect(Collectors.toSet()));
        }
        
        // Map scores
        if (race.getScores() != null) {
            dto.setScores(race.getScores().stream()
                .map(ScoreMapper::toScoreResponseDto)
                .collect(Collectors.toSet()));
        }
        
        // Map attributions
        if (race.getAttributions() != null) {
            dto.setAttributions(race.getAttributions().stream()
                .map(RaceMapper::toAttributionDto)
                .collect(Collectors.toSet()));
        }
        
        return dto;
    }
    
    private static RaceResponseDto.CarDto toCarDto(Car car) {
        if (car == null) {
            return null;
        }
        return RaceResponseDto.CarDto.builder()
            .id(car.getId())
            .name(car.getName())
            .imageUrl(car.getImageUrl())
            .build();
    }
    
    private static RaceResponseDto.CardDto toCardDto(Card card) {
        if (card == null) {
            return null;
        }
        return RaceResponseDto.CardDto.builder()
            .id(card.getId())
            .location(card.getLocation())
            .track(card.getTrack())
            .imageUrl(card.getImageUrl())
            .build();
    }
    
    private static RaceResponseDto.RaceParameterDto toRaceParameterDto(RaceParameters param) {
        if (param == null) {
            return null;
        }
        return RaceResponseDto.RaceParameterDto.builder()
            .id(param.getId())
            .name(param.getName())
            .isActive(param.getIsActive())
            .downloadUrl(param.getDownloadUrl())
            .build();
    }
    
    private static RaceResponseDto.AttributionDto toAttributionDto(Attribution attribution) {
        if (attribution == null) {
            return null;
        }
        return RaceResponseDto.AttributionDto.builder()
            .id(attribution.getId())
            .user(attribution.getUser() != null ? UserResponseMapper.toUserResponseDto(attribution.getUser()) : null)
            .car(toCarDto(attribution.getCar()))
            .notes(attribution.getNotes())
            .build();
    }

    public static Set<RaceResponseDto> toRaceResponseDtoList(List<Race> races) {
        if (races == null) {
            return new HashSet<>();
        }
        return races.stream()
            .map(RaceMapper::toRaceResponseDto)
            .collect(Collectors.toSet());
    }
}
