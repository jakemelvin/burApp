package com.packt.blurApp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.packt.blurApp.mapper.raceMapper.RaceMapper;
import com.packt.blurApp.model.Race;
import com.packt.blurApp.model.enums.AttributionType;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.race.IRaceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/races")
@RequiredArgsConstructor
public class RaceController {
    private final IRaceService raceService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_RACE')")
    public ResponseEntity<ApiResponse<?>> getRaceById(@PathVariable Long id) {
        log.info("GET ${api.prefix}/races/{} - Get race by ID", id);
        Race race = raceService.getRaceById(id);
        return ResponseEntity.ok(ApiResponse.success("Race fetched successfully",
                RaceMapper.toRaceResponseDto(race)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_RACE')")
    public ResponseEntity<ApiResponse<?>> getAllRaces() {
        log.info("GET ${api.prefix}/races - Get all races");
        return ResponseEntity.ok(ApiResponse.success("Races fetched successfully",
                RaceMapper.toRaceResponseDtoList(raceService.getAllRaces())));
    }

    @GetMapping("/count")
    @PreAuthorize("hasAuthority('VIEW_RACE')")
    public ResponseEntity<ApiResponse<?>> getTotalRacesCount() {
        log.info("GET ${api.prefix}/races/count - Get total races count");
        return ResponseEntity.ok(ApiResponse.success(
                "Races count fetched successfully",
                new com.packt.blurApp.dto.Race.RaceCountDto(raceService.getTotalRacesCount())));
    }

    @GetMapping("/party/{partyId}")
    @PreAuthorize("hasAuthority('VIEW_RACE')")
    public ResponseEntity<ApiResponse<?>> getRacesByPartyId(@PathVariable Long partyId) {
        log.info("GET ${api.prefix}/races/party/{} - Get races by party ID", partyId);
        return ResponseEntity.ok(ApiResponse.success("Races fetched successfully",
                RaceMapper.toRaceResponseDtoList(raceService.getRacesByPartyId(partyId))));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('VIEW_RACE')")
    public ResponseEntity<ApiResponse<?>> getRacesByStatus(@PathVariable String status) {
        log.info("GET ${api.prefix}/races/status/{} - Get races by status", status);
        return ResponseEntity.ok(ApiResponse.success("Races fetched successfully",
                RaceMapper.toRaceResponseDtoList(raceService.getRacesByStatus(status))));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_RACE')")
    public ResponseEntity<ApiResponse<?>> createRace(
            @RequestParam Long partyId,
            @RequestParam(defaultValue = "PER_USER") String attributionType) {
        log.info("POST ${api.prefix}/races - Create race for party {} with attribution {}", partyId, attributionType);
        AttributionType type = AttributionType.valueOf(attributionType.toUpperCase());
        Race race = raceService.createRace(partyId, type);
        return ResponseEntity.ok(ApiResponse.success("Race created successfully",
                RaceMapper.toRaceResponseDto(race)));
    }

    @PostMapping("/{raceId}/participants/{userId}")
    @PreAuthorize("hasAuthority('JOIN_RACE')")
    public ResponseEntity<ApiResponse<?>> addParticipant(@PathVariable Long raceId, @PathVariable Long userId) {
        log.info("POST ${api.prefix}/races/{}/participants/{} - Add participant", raceId, userId);
        Race race = raceService.addParticipant(raceId, userId);
        return ResponseEntity.ok(ApiResponse.success("Participant added successfully",
                RaceMapper.toRaceResponseDto(race)));
    }

    @DeleteMapping("/{raceId}/participants/{userId}")
    @PreAuthorize("hasAuthority('LEAVE_RACE')")
    public ResponseEntity<ApiResponse<?>> removeParticipant(@PathVariable Long raceId, @PathVariable Long userId) {
        log.info("DELETE ${api.prefix}/races/{}/participants/{} - Remove participant", raceId, userId);
        Race race = raceService.removeParticipant(raceId, userId);
        return ResponseEntity.ok(ApiResponse.success("Participant removed successfully",
                RaceMapper.toRaceResponseDto(race)));
    }

    @PostMapping("/{raceId}/start")
    @PreAuthorize("hasAuthority('START_RACE')")
    public ResponseEntity<ApiResponse<?>> startRace(@PathVariable Long raceId) {
        log.info("POST ${api.prefix}/races/{}/start - Start race", raceId);
        Race race = raceService.startRace(raceId);
        return ResponseEntity.ok(ApiResponse.success("Race started successfully",
                RaceMapper.toRaceResponseDto(race)));
    }

    @PostMapping("/{raceId}/complete")
    @PreAuthorize("hasAuthority('START_RACE')")
    public ResponseEntity<ApiResponse<?>> completeRace(@PathVariable Long raceId) {
        log.info("POST ${api.prefix}/races/{}/complete - Complete race", raceId);
        Race race = raceService.completeRace(raceId);
        return ResponseEntity.ok(ApiResponse.success("Race completed successfully",
                RaceMapper.toRaceResponseDto(race)));
    }

    @PostMapping("/{raceId}/cancel")
    @PreAuthorize("hasAuthority('DELETE_RACE')")
    public ResponseEntity<ApiResponse<?>> cancelRace(@PathVariable Long raceId) {
        log.info("POST ${api.prefix}/races/{}/cancel - Cancel race", raceId);
        Race race = raceService.cancelRace(raceId);
        return ResponseEntity.ok(ApiResponse.success("Race cancelled successfully",
                RaceMapper.toRaceResponseDto(race)));
    }
}
