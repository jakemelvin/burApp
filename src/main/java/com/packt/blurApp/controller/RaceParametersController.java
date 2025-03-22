package com.packt.blurApp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.raceParameters.IRaceParametersService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/raceParameters")
@RequiredArgsConstructor
public class RaceParametersController {
  private final IRaceParametersService raceParametersService;

  @GetMapping
  public ResponseEntity<ApiResponse> getAllRaceParameters() {
    return ResponseEntity
        .ok(new ApiResponse("Get all race parameters successful", raceParametersService.getAllRaceParameters()));
  }

  @GetMapping("/get-by-id")
  public ResponseEntity<ApiResponse> getRaceParameterById(@RequestParam Long raceParameterId) {
    try {
      return ResponseEntity.ok(new ApiResponse("Get race parameter successful",
          raceParametersService.getRaceParameterById(raceParameterId)));
    } catch (ResourceNotFoundExceptions e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }
}
