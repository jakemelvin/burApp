package com.packt.blurApp.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.packt.blurApp.dto.Party.AddPartyMemberDto;
import com.packt.blurApp.dto.Party.PartyMemberDto;
import com.packt.blurApp.dto.Party.UpdatePartyMemberRoleDto;
import com.packt.blurApp.mapper.partyMapper.PartyMapper;
import com.packt.blurApp.model.Party;
import com.packt.blurApp.model.enums.PartyRole;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.party.PartyService;
import com.packt.blurApp.service.user.IUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/parties")
@RequiredArgsConstructor
public class PartyController {
    private final PartyService partyService;
    private final IUserService userService;

    @GetMapping("/today")
    @PreAuthorize("hasAuthority('VIEW_PARTY')")
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

    // ==================== PARTY MEMBER MANAGEMENT ENDPOINTS ====================

    /**
     * Get all party members with their roles
     */
    @GetMapping("/{partyId}/members/roles")
    @PreAuthorize("hasAuthority('VIEW_PARTY')")
    public ResponseEntity<ApiResponse<?>> getPartyMembersWithRoles(@PathVariable Long partyId) {
        log.info("GET /parties/{}/members/roles - Get party members with roles", partyId);
        List<PartyMemberDto> members = partyService.getPartyMembersWithRoles(partyId);
        return ResponseEntity.ok(ApiResponse.success("Party members fetched successfully", members));
    }

    /**
     * Add a new member to the party
     */
    @PostMapping("/{partyId}/members")
    @PreAuthorize("hasAuthority('MANAGE_PARTY')")
    public ResponseEntity<ApiResponse<?>> addPartyMember(
            @PathVariable Long partyId,
            @Valid @RequestBody AddPartyMemberDto dto) {
        log.info("POST /parties/{}/members - Add member to party", partyId);
        PartyMemberDto member = partyService.addPartyMember(partyId, dto);
        return ResponseEntity.ok(ApiResponse.success("Member added successfully", member));
    }

    /**
     * Update a member's role (promote to CO_HOST or demote to PARTICIPANT)
     */
    @PatchMapping("/{partyId}/members/{userId}/role")
    @PreAuthorize("hasAuthority('MANAGE_PARTY')")
    public ResponseEntity<ApiResponse<?>> updateMemberRole(
            @PathVariable Long partyId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdatePartyMemberRoleDto dto) {
        log.info("PATCH /parties/{}/members/{}/role - Update member role", partyId, userId);
        PartyMemberDto member = partyService.updateMemberRole(partyId, userId, dto);
        return ResponseEntity.ok(ApiResponse.success("Member role updated successfully", member));
    }

    /**
     * Remove a member from the party
     */
    @DeleteMapping("/{partyId}/members/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_PARTY')")
    public ResponseEntity<ApiResponse<?>> removeMember(
            @PathVariable Long partyId,
            @PathVariable Long userId) {
        log.info("DELETE /parties/{}/members/{} - Remove member from party", partyId, userId);
        partyService.removeMember(partyId, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully"));
    }

    /**
     * Transfer party ownership to another member (HOST only)
     */
    @PostMapping("/{partyId}/transfer-ownership/{newHostId}")
    @PreAuthorize("hasAuthority('MANAGE_PARTY')")
    public ResponseEntity<ApiResponse<?>> transferOwnership(
            @PathVariable Long partyId,
            @PathVariable Long newHostId) {
        log.info("POST /parties/{}/transfer-ownership/{} - Transfer ownership", partyId, newHostId);
        PartyMemberDto newHost = partyService.transferOwnership(partyId, newHostId);
        return ResponseEntity.ok(ApiResponse.success("Ownership transferred successfully", newHost));
    }

    /**
     * Check if current user can manage the party
     */
    @GetMapping("/{partyId}/can-manage")
    @PreAuthorize("hasAuthority('VIEW_PARTY')")
    public ResponseEntity<ApiResponse<?>> canCurrentUserManageParty(@PathVariable Long partyId) {
        log.info("GET /parties/{}/can-manage - Check if user can manage party", partyId);
        boolean canManage = partyService.canCurrentUserManageParty(partyId);
        return ResponseEntity.ok(ApiResponse.success("Permission check successful", Map.of("canManage", canManage)));
    }

    /**
     * Get current user's role in the party
     */
    @GetMapping("/{partyId}/my-role")
    @PreAuthorize("hasAuthority('VIEW_PARTY')")
    public ResponseEntity<ApiResponse<?>> getCurrentUserRole(@PathVariable Long partyId) {
        log.info("GET /parties/{}/my-role - Get current user's role in party", partyId);
        PartyRole role = partyService.getCurrentUserRole(partyId);
        return ResponseEntity.ok(ApiResponse.success("Role fetched successfully", 
                Map.of("role", role != null ? role.name() : "NOT_MEMBER")));
    }

    /**
     * Promote a member to co-host (convenience endpoint)
     */
    @PostMapping("/{partyId}/co-hosts/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_PARTY')")
    public ResponseEntity<ApiResponse<?>> promoteToCoHost(
            @PathVariable Long partyId,
            @PathVariable Long userId) {
        log.info("POST /parties/{}/co-hosts/{} - Promote to co-host", partyId, userId);
        Party party = partyService.assignManager(partyId, userId);
        return ResponseEntity.ok(ApiResponse.success("User promoted to co-host successfully",
                PartyMapper.toPartyGetResponseDto(party)));
    }

    /**
     * Demote a co-host to participant (convenience endpoint)
     */
    @DeleteMapping("/{partyId}/co-hosts/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_PARTY')")
    public ResponseEntity<ApiResponse<?>> demoteCoHost(
            @PathVariable Long partyId,
            @PathVariable Long userId) {
        log.info("DELETE /parties/{}/co-hosts/{} - Demote co-host", partyId, userId);
        Party party = partyService.removeManager(partyId, userId);
        return ResponseEntity.ok(ApiResponse.success("Co-host demoted to participant successfully",
                PartyMapper.toPartyGetResponseDto(party)));
    }
}
