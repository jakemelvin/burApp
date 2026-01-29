package com.packt.blurApp.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.packt.blurApp.mapper.partyMapper.PartyMapper;
import com.packt.blurApp.model.Party;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.party.IPartyService;
import com.packt.blurApp.service.user.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/parties")
@RequiredArgsConstructor
public class PartyController {
    private final IPartyService partyService;
    private final IUserService userService;

    @GetMapping("/today")
    @PreAuthorize("hasAuthority('JOIN_PARTY')")
    public ResponseEntity<ApiResponse<?>> getTodayPartyOrCreate() {
        log.info("GET ${api.prefix}/parties/today - Get or create today's party");
        Party party = partyService.getTodayPartyOrCreate();
        return ResponseEntity.ok(ApiResponse.success("Party retrieved successfully",
                PartyMapper.toPartyGetResponseDto(party)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_PARTY')")
    public ResponseEntity<ApiResponse<?>> getPartyById(@PathVariable Long id) {
        log.info("GET ${api.prefix}/parties/{} - Get party by ID", id);
        Party party = partyService.getPartyById(id);
        return ResponseEntity.ok(ApiResponse.success("Party fetched successfully",
                PartyMapper.toPartyGetResponseDto(party)));
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasAuthority('VIEW_PARTY')")
    public ResponseEntity<ApiResponse<?>> getPartyByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET ${api.prefix}/parties/date/{} - Get party by date", date);
        Party party = partyService.getPartyByDate(date);
        return ResponseEntity.ok(ApiResponse.success("Party fetched successfully",
                PartyMapper.toPartyGetResponseDto(party)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_PARTY')")
    public ResponseEntity<ApiResponse<?>> getAllParties() {
        log.info("GET ${api.prefix}/parties - Get all parties");
        return ResponseEntity.ok(ApiResponse.success("Parties fetched successfully",
                PartyMapper.toPartyResponseDtoList(partyService.getAllParties())));
    }

    @PostMapping("/{partyId}/join")
    @PreAuthorize("hasAuthority('JOIN_PARTY')")
    public ResponseEntity<ApiResponse<?>> joinParty(@PathVariable Long partyId) {
        log.info("POST ${api.prefix}/parties/{}/join - Join party", partyId);
        Party party = partyService.joinParty(partyId, userService.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success("Joined party successfully",
                PartyMapper.toPartyGetResponseDto(party)));
    }

    @PostMapping("/{partyId}/leave")
    @PreAuthorize("hasAuthority('JOIN_PARTY')")
    public ResponseEntity<ApiResponse<?>> leaveParty(@PathVariable Long partyId) {
        log.info("POST ${api.prefix}/parties/{}/leave - Leave party", partyId);
        Party party = partyService.leaveParty(partyId, userService.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success("Left party successfully",
                PartyMapper.toPartyGetResponseDto(party)));
    }

    @PostMapping("/{partyId}/managers/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_PARTY')")
    public ResponseEntity<ApiResponse<?>> assignManager(@PathVariable Long partyId, @PathVariable Long userId) {
        log.info("POST ${api.prefix}/parties/{}/managers/{} - Assign manager", partyId, userId);
        Party party = partyService.assignManager(partyId, userId);
        return ResponseEntity.ok(ApiResponse.success("Manager assigned successfully",
                PartyMapper.toPartyGetResponseDto(party)));
    }

    @DeleteMapping("/{partyId}/managers/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_PARTY')")
    public ResponseEntity<ApiResponse<?>> removeManager(@PathVariable Long partyId, @PathVariable Long userId) {
        log.info("DELETE ${api.prefix}/parties/{}/managers/{} - Remove manager", partyId, userId);
        Party party = partyService.removeManager(partyId, userId);
        return ResponseEntity.ok(ApiResponse.success("Manager removed successfully",
                PartyMapper.toPartyGetResponseDto(party)));
    }

    @GetMapping("/{id}/active")
    @PreAuthorize("hasAuthority('VIEW_PARTY')")
    public ResponseEntity<ApiResponse<?>> getPartyActiveStatus(@PathVariable Long id) {
        log.info("GET ${api.prefix}/parties/{}/active - Get party active status", id);
        return ResponseEntity.ok(ApiResponse.success(
                "Party active status fetched successfully",
                partyService.getPartyActiveStatus(id)));
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasAuthority('VIEW_PARTY')")
    public ResponseEntity<ApiResponse<?>> getPartyMembers(@PathVariable Long id) {
        log.info("GET ${api.prefix}/parties/{}/members - Get party members", id);
        java.util.Set<com.packt.blurApp.dto.User.UserResponseDto> members = partyService.getPartyMembers(id)
                .stream()
                .map(u -> new com.packt.blurApp.dto.User.UserResponseDto(u.getId(), u.getUsername()))
                .collect(java.util.stream.Collectors.toSet());
        return ResponseEntity.ok(ApiResponse.success("Party members fetched successfully", members));
    }

    @DeleteMapping("/{partyId}")
    @PreAuthorize("hasAuthority('DELETE_PARTY')")
    public ResponseEntity<ApiResponse<?>> deactivateParty(@PathVariable Long partyId) {
        log.info("DELETE ${api.prefix}/parties/{} - Deactivate party", partyId);
        partyService.deactivateParty(partyId);
        return ResponseEntity.ok(ApiResponse.success("Party deactivated successfully"));
    }
}
