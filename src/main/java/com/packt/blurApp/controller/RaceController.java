package com.packt.blurApp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.packt.blurApp.dto.RaceParameters.AddRaceParametersDto;
import com.packt.blurApp.dto.User.RacePlayersDto;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.race.IRaceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/races")
public class RaceController {
  private final IRaceService raceService;

  @GetMapping("/get-by-id")
  public ResponseEntity<ApiResponse> getRaceById(@RequestParam Long raceId) {
    return ResponseEntity.ok(new ApiResponse("Race fetched successfully", raceService.getRaceById(raceId)));
  }

  @PostMapping("/create-race")
  public ResponseEntity<ApiResponse> createRace(@RequestParam Long partyId,
      @RequestBody List<AddRaceParametersDto> raceParametersDtos) {
    try {
      return ResponseEntity
          .ok(new ApiResponse("Race created Successfully", raceService.createRace(partyId, raceParametersDtos)));
    } catch (ResourceNotFoundExceptions e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @PutMapping("/update-race")
  public ResponseEntity<ApiResponse> updateRacePlayers(@RequestParam Long raceId,
      @RequestBody List<RacePlayersDto> racePlayers) {
    try {
      return ResponseEntity
          .ok(new ApiResponse("Race updated Successfully", raceService.updateRacePlayers(racePlayers, raceId)));

    } catch (ResourceNotFoundExceptions e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

}
