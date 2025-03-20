package com.packt.blurApp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.packt.blurApp.dto.Party.PartyUpdateDto;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.mapper.partyMapper.PartyMapper;
import com.packt.blurApp.model.Party;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.party.IPartyService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/parties")
public class PartyController {

  private final IPartyService partyService;

  @GetMapping
  public ResponseEntity<ApiResponse> getAllParties() {
    List<Party> parties = partyService.getAllParties();
    return ResponseEntity.ok(new ApiResponse("Parties fetched", PartyMapper.toPartyGetResponseDtoList(parties)));
  }

  @GetMapping("/get-party/{partyId}")
  public ResponseEntity<ApiResponse> getPartyById(@PathVariable Long partyId) {
    try {
      Party party = partyService.getPartyById(partyId);
      return ResponseEntity.ok(new ApiResponse("Party fetched successfully", PartyMapper.toPartyGetResponseDto(party)));
    } catch (ResourceNotFoundExceptions e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @PostMapping
  public ResponseEntity<ApiResponse> createParty() {
    Party createdParty = partyService.createParty();
    return ResponseEntity
        .ok(new ApiResponse("Party created successfully", PartyMapper.toPartyGetResponseDto(createdParty)));
  }

  @PutMapping("/update")
  public ResponseEntity<ApiResponse> updateParty(@RequestBody PartyUpdateDto updatedParty, @RequestParam Long partyId) {
    try {
      Party theParty = partyService.updateParty(updatedParty, partyId);
      return ResponseEntity
          .ok(new ApiResponse("Party updated successfully", PartyMapper.toPartyGetResponseDto(theParty)));
    } catch (ResourceNotFoundExceptions e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }
}
