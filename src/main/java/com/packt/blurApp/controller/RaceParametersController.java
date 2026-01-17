package com.packt.blurApp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.raceParameters.IRaceParametersService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/raceParameters")
@RequiredArgsConstructor
public class RaceParametersController {
    private final IRaceParametersService raceParametersService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_RACE')")
    public ResponseEntity<ApiResponse<?>> getAllRaceParameters() {
        return ResponseEntity.ok(ApiResponse.success("Get all race parameters successful", 
            raceParametersService.getAllRaceParameters()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_RACE')")
    public ResponseEntity<ApiResponse<?>> getRaceParameterById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Get race parameter successful",
            raceParametersService.getRaceParameterById(id)));
    }
}
