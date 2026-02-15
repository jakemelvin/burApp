package com.packt.blurApp.service.party;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packt.blurApp.dto.Party.AddPartyMemberDto;
import com.packt.blurApp.dto.Party.PartyMemberDto;
import com.packt.blurApp.dto.Party.UpdatePartyMemberRoleDto;
import com.packt.blurApp.exceptions.BadRequestException;
import com.packt.blurApp.exceptions.ConflictException;
import com.packt.blurApp.exceptions.ForbiddenException;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.Party;
import com.packt.blurApp.model.PartyMember;
import com.packt.blurApp.model.User;
import com.packt.blurApp.model.enums.PartyRole;
import com.packt.blurApp.repository.PartyMemberRepository;
import com.packt.blurApp.repository.PartyRepository;
import com.packt.blurApp.repository.UserRepository;
import com.packt.blurApp.service.user.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartyService implements IPartyService {

    private final PartyRepository partyRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final UserRepository userRepository;
    private final IUserService userService;

    @Override
    @Transactional
    public Party getTodayPartyOrCreate() {
        log.info("Getting or creating today's party");

        LocalDate today = LocalDate.now();
        User currentUser = userService.getCurrentUser();

        // Check if party already exists for today
        Party party = partyRepository.findByPartyDateAndActiveTrue(today)
                .orElseGet(() -> {
                    log.info("No party exists for today. Creating new party by user: {}", currentUser.getUsername());

                    // Create new party - first user to arrive creates it
                    Party newParty = Party.builder()
                            .partyDate(today)
                            .creator(currentUser)
                            .active(true)
                            .build();

                    Party savedParty = partyRepository.save(newParty);

                    // Creator automatically becomes HOST
                    PartyMember hostMember = PartyMember.builder()
                            .party(savedParty)
                            .user(currentUser)
                            .role(PartyRole.HOST)
                            .build();
                    partyMemberRepository.save(hostMember);
                    
                    log.info("Party created successfully for date: {} by user: {}", today, currentUser.getUsername());

                    return savedParty;
                });

        // Auto-join: users become PARTICIPANT when they access the party
        if (!partyMemberRepository.existsByPartyAndUser(party, currentUser)) {
            PartyMember participantMember = PartyMember.builder()
                    .party(party)
                    .user(currentUser)
                    .role(PartyRole.PARTICIPANT)
                    .build();
            partyMemberRepository.save(participantMember);
            log.info("User {} auto-joined party {} as PARTICIPANT", currentUser.getUsername(), party.getId());
        }

        // Initialize lazy-loaded relationships for DTO mapping
        if (party.getCreator() != null) {
            org.hibernate.Hibernate.initialize(party.getCreator());
        }
        org.hibernate.Hibernate.initialize(party.getPartyMembers());
        
        // Initialize users within party members
        party.getPartyMembers().forEach(pm -> {
            org.hibernate.Hibernate.initialize(pm.getUser());
            if (pm.getInvitedBy() != null) {
                org.hibernate.Hibernate.initialize(pm.getInvitedBy());
            }
        });

        return party;
    }

    @Override
    @Transactional(readOnly = true)
    public Party getPartyById(Long id) {
        log.debug("Fetching party by ID: {}", id);
        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptions("Party not found with ID: " + id));
        
        // Initialize lazy-loaded relationships for DTO mapping
        if (party.getCreator() != null) {
            org.hibernate.Hibernate.initialize(party.getCreator());
        }
        org.hibernate.Hibernate.initialize(party.getPartyMembers());
        
        // Initialize users within party members
        party.getPartyMembers().forEach(pm -> {
            org.hibernate.Hibernate.initialize(pm.getUser());
            if (pm.getInvitedBy() != null) {
                org.hibernate.Hibernate.initialize(pm.getInvitedBy());
            }
        });
        
        return party;
    }

    @Override
    @Transactional(readOnly = true)
    public Party getPartyByDate(LocalDate date) {
        log.debug("Fetching party by date: {}", date);
        Party party = partyRepository.findByPartyDate(date)
                .orElseThrow(() -> new ResourceNotFoundExceptions("No party found for date: " + date));
        
        // Initialize lazy-loaded relationships for DTO mapping
        if (party.getCreator() != null) {
            org.hibernate.Hibernate.initialize(party.getCreator());
        }
        org.hibernate.Hibernate.initialize(party.getPartyMembers());
        
        // Initialize users within party members
        party.getPartyMembers().forEach(pm -> {
            org.hibernate.Hibernate.initialize(pm.getUser());
            if (pm.getInvitedBy() != null) {
                org.hibernate.Hibernate.initialize(pm.getInvitedBy());
            }
        });
        
        return party;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Party> getAllParties() {
        log.debug("Fetching all parties");
        List<Party> parties = partyRepository.findAll();
        
        // Initialize party members for DTO mapping
        parties.forEach(party -> {
            org.hibernate.Hibernate.initialize(party.getPartyMembers());
        });
        
        return parties;
    }

    @Override
    @Transactional
    public Party joinParty(Long partyId, User user) {
        log.info("User {} joining party {}", user.getUsername(), partyId);
        
        Party party = getPartyById(partyId);
        
        if (!party.isActive()) {
            throw new BadRequestException("Cannot join inactive party");
        }
        
        if (partyMemberRepository.existsByPartyAndUser(party, user)) {
            throw new ConflictException("User is already a member of this party");
        }
        
        PartyMember newMember = PartyMember.builder()
                .party(party)
                .user(user)
                .role(PartyRole.PARTICIPANT)
                .invitedBy(userService.getCurrentUser())
                .build();
        partyMemberRepository.save(newMember);
        
        log.info("User {} joined party {} successfully as PARTICIPANT", user.getUsername(), partyId);
        return getPartyById(partyId); // Refresh to get updated members
    }

    @Override
    @Transactional
    public Party leaveParty(Long partyId, User user) {
        log.info("User {} leaving party {}", user.getUsername(), partyId);
        
        Party party = getPartyById(partyId);
        
        PartyMember membership = partyMemberRepository.findByPartyAndUser(party, user)
                .orElseThrow(() -> new BadRequestException("User is not a member of this party"));
        
        // Host cannot leave their own party
        if (membership.isHost()) {
            throw new ForbiddenException("Party host cannot leave the party. Transfer ownership first or deactivate the party.");
        }
        
        partyMemberRepository.delete(membership);
        
        log.info("User {} left party {} successfully", user.getUsername(), partyId);
        return getPartyById(partyId);
    }

    @Override
    @Transactional
    public Party assignManager(Long partyId, Long userId) {
        log.info("Assigning user {} as co-host of party {}", userId, partyId);
        
        User currentUser = userService.getCurrentUser();
        Party party = getPartyById(partyId);
        
        // Only host can assign co-hosts
        if (!party.isHost(currentUser) && !isGreatAdmin(currentUser)) {
            throw new ForbiddenException("Only the party host can assign co-hosts");
        }
        
        PartyMember memberToPromote = partyMemberRepository.findByPartyIdAndUserId(partyId, userId)
                .orElseThrow(() -> new BadRequestException("User must be a party member to become a co-host"));
        
        if (memberToPromote.isHost()) {
            throw new BadRequestException("Cannot change host's role");
        }
        
        if (memberToPromote.isCoHost()) {
            throw new ConflictException("User is already a co-host of this party");
        }
        
        memberToPromote.setRole(PartyRole.CO_HOST);
        partyMemberRepository.save(memberToPromote);
        
        log.info("User {} assigned as CO_HOST of party {}", userId, partyId);
        return getPartyById(partyId);
    }

    @Override
    @Transactional
    public Party removeManager(Long partyId, Long userId) {
        log.info("Removing user {} as co-host of party {}", userId, partyId);
        
        User currentUser = userService.getCurrentUser();
        Party party = getPartyById(partyId);
        
        // Only host can remove co-hosts
        if (!party.isHost(currentUser) && !isGreatAdmin(currentUser)) {
            throw new ForbiddenException("Only the party host can remove co-hosts");
        }
        
        PartyMember memberToDemote = partyMemberRepository.findByPartyIdAndUserId(partyId, userId)
                .orElseThrow(() -> new ResourceNotFoundExceptions("User not found in this party"));
        
        // Cannot demote host
        if (memberToDemote.isHost()) {
            throw new BadRequestException("Cannot demote the host");
        }
        
        if (!memberToDemote.isCoHost()) {
            throw new BadRequestException("User is not a co-host of this party");
        }
        
        memberToDemote.setRole(PartyRole.PARTICIPANT);
        partyMemberRepository.save(memberToDemote);
        
        log.info("User {} demoted to PARTICIPANT in party {}", userId, partyId);
        return getPartyById(partyId);
    }
    
    // Helper method to check if user is GREAT_ADMIN
    private boolean isGreatAdmin(User user) {
        return user.getRole() != null && "GREAT_ADMIN".equals(user.getRole().getName());
    }

    @Override
    @Transactional
    public void deactivateParty(Long partyId) {
        log.info("Deactivating party {}", partyId);
        
        User currentUser = userService.getCurrentUser();
        Party party = getPartyById(partyId);
        
        // Only host or GREAT_ADMIN can deactivate party
        if (!party.isHost(currentUser) && !isGreatAdmin(currentUser)) {
            throw new ForbiddenException("Only party host can deactivate the party");
        }
        
        party.setActive(false);
        partyRepository.save(party);
        
        log.info("Party {} deactivated successfully", partyId);
    }

    @Override
    @Transactional(readOnly = true)
    public com.packt.blurApp.dto.Party.PartyActiveStatusDto getPartyActiveStatus(Long partyId) {
        Party party = getPartyById(partyId);

        LocalDate today = LocalDate.now();
        LocalDate partyDate = party.getPartyDate();

        boolean active = party.isActive();
        boolean isToday = partyDate != null && partyDate.equals(today);
        boolean actionable = active && isToday;

        String reason;
        if (!active) {
            reason = "PARTY_DEACTIVATED";
        } else if (partyDate == null) {
            reason = "PARTY_DATE_MISSING";
        } else if (!isToday) {
            reason = "PARTY_DATE_NOT_TODAY";
        } else {
            reason = "OK";
        }

        return new com.packt.blurApp.dto.Party.PartyActiveStatusDto(
                party.getId(),
                active,
                actionable,
                partyDate,
                today,
                reason);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<User> getPartyMembers(Long partyId) {
        Party party = getPartyById(partyId);
        return party.getMembers();
    }
    
    // ==================== NEW METHODS FOR PARTY MEMBER MANAGEMENT ====================
    
    /**
     * Get all party members with their roles
     */
    @Transactional(readOnly = true)
    public List<PartyMemberDto> getPartyMembersWithRoles(Long partyId) {
        log.debug("Fetching party members with roles for party {}", partyId);
        
        List<PartyMember> members = partyMemberRepository.findByPartyId(partyId);
        
        return members.stream()
                .map(this::toPartyMemberDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Add a new member to the party
     */
    @Transactional
    public PartyMemberDto addPartyMember(Long partyId, AddPartyMemberDto dto) {
        log.info("Adding user {} to party {} with role {}", dto.getUserId(), partyId, dto.getRole());
        
        User currentUser = userService.getCurrentUser();
        Party party = getPartyById(partyId);
        
        // Only host or co-hosts can add members
        if (!party.canManage(currentUser)) {
            throw new ForbiddenException("Only party managers can add members");
        }
        
        // Cannot assign HOST role - only one host per party
        if (dto.getRole() == PartyRole.HOST) {
            throw new BadRequestException("Cannot assign HOST role. Use transfer ownership instead.");
        }
        
        User userToAdd = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundExceptions("User not found with ID: " + dto.getUserId()));
        
        if (partyMemberRepository.existsByPartyAndUser(party, userToAdd)) {
            throw new ConflictException("User is already a member of this party");
        }
        
        // Only host can add co-hosts
        if (dto.getRole() == PartyRole.CO_HOST && !party.isHost(currentUser) && !isGreatAdmin(currentUser)) {
            throw new ForbiddenException("Only the party host can add co-hosts");
        }
        
        PartyMember newMember = PartyMember.builder()
                .party(party)
                .user(userToAdd)
                .role(dto.getRole() != null ? dto.getRole() : PartyRole.PARTICIPANT)
                .invitedBy(currentUser)
                .build();
        
        PartyMember savedMember = partyMemberRepository.save(newMember);
        log.info("User {} added to party {} as {}", dto.getUserId(), partyId, savedMember.getRole());
        
        return toPartyMemberDto(savedMember);
    }
    
    /**
     * Update a member's role
     */
    @Transactional
    public PartyMemberDto updateMemberRole(Long partyId, Long userId, UpdatePartyMemberRoleDto dto) {
        log.info("Updating role for user {} in party {} to {}", userId, partyId, dto.getRole());
        
        User currentUser = userService.getCurrentUser();
        Party party = getPartyById(partyId);
        
        // Only host can change roles
        if (!party.isHost(currentUser) && !isGreatAdmin(currentUser)) {
            throw new ForbiddenException("Only the party host can change member roles");
        }
        
        PartyMember member = partyMemberRepository.findByPartyIdAndUserId(partyId, userId)
                .orElseThrow(() -> new ResourceNotFoundExceptions("Member not found in this party"));
        
        // Cannot change host's role
        if (member.isHost()) {
            throw new BadRequestException("Cannot change host's role. Use transfer ownership instead.");
        }
        
        // Cannot assign HOST role
        if (dto.getRole() == PartyRole.HOST) {
            throw new BadRequestException("Cannot assign HOST role. Use transfer ownership instead.");
        }
        
        member.setRole(dto.getRole());
        PartyMember updatedMember = partyMemberRepository.save(member);
        
        log.info("User {} role updated to {} in party {}", userId, dto.getRole(), partyId);
        return toPartyMemberDto(updatedMember);
    }
    
    /**
     * Remove a member from the party
     */
    @Transactional
    public void removeMember(Long partyId, Long userId) {
        log.info("Removing user {} from party {}", userId, partyId);
        
        User currentUser = userService.getCurrentUser();
        Party party = getPartyById(partyId);
        
        // Only host or co-hosts can remove members
        if (!party.canManage(currentUser)) {
            throw new ForbiddenException("Only party managers can remove members");
        }
        
        PartyMember member = partyMemberRepository.findByPartyIdAndUserId(partyId, userId)
                .orElseThrow(() -> new ResourceNotFoundExceptions("Member not found in this party"));
        
        // Cannot remove host
        if (member.isHost()) {
            throw new BadRequestException("Cannot remove the host from the party");
        }
        
        // Co-hosts can only remove participants, not other co-hosts
        if (member.isCoHost() && !party.isHost(currentUser) && !isGreatAdmin(currentUser)) {
            throw new ForbiddenException("Only the host can remove co-hosts");
        }
        
        partyMemberRepository.delete(member);
        log.info("User {} removed from party {}", userId, partyId);
    }
    
    /**
     * Transfer party ownership to another member
     */
    @Transactional
    public PartyMemberDto transferOwnership(Long partyId, Long newHostId) {
        log.info("Transferring ownership of party {} to user {}", partyId, newHostId);
        
        User currentUser = userService.getCurrentUser();
        Party party = getPartyById(partyId);
        
        // Only current host or GREAT_ADMIN can transfer ownership
        if (!party.isHost(currentUser) && !isGreatAdmin(currentUser)) {
            throw new ForbiddenException("Only the party host can transfer ownership");
        }
        
        PartyMember newHost = partyMemberRepository.findByPartyIdAndUserId(partyId, newHostId)
                .orElseThrow(() -> new BadRequestException("User must be a party member to become host"));
        
        if (newHost.isHost()) {
            throw new BadRequestException("User is already the host");
        }
        
        // Find current host and demote to co-host
        PartyMember currentHost = partyMemberRepository.findHostByPartyId(partyId)
                .orElse(null);
        
        if (currentHost != null) {
            currentHost.setRole(PartyRole.CO_HOST);
            partyMemberRepository.save(currentHost);
        }
        
        // Promote new host
        newHost.setRole(PartyRole.HOST);
        PartyMember savedNewHost = partyMemberRepository.save(newHost);
        
        // Update party creator reference
        party.setCreator(newHost.getUser());
        partyRepository.save(party);
        
        log.info("Ownership of party {} transferred to user {}", partyId, newHostId);
        return toPartyMemberDto(savedNewHost);
    }
    
    /**
     * Check if current user can manage the party
     */
    @Transactional(readOnly = true)
    public boolean canCurrentUserManageParty(Long partyId) {
        User currentUser = userService.getCurrentUser();
        if (isGreatAdmin(currentUser)) {
            return true;
        }
        return partyMemberRepository.canUserManageParty(partyId, currentUser.getId());
    }
    
    /**
     * Get current user's role in the party
     */
    @Transactional(readOnly = true)
    public PartyRole getCurrentUserRole(Long partyId) {
        User currentUser = userService.getCurrentUser();
        return partyMemberRepository.findByPartyIdAndUserId(partyId, currentUser.getId())
                .map(PartyMember::getRole)
                .orElse(null);
    }
    
    // Helper method to convert PartyMember to DTO
    private PartyMemberDto toPartyMemberDto(PartyMember member) {
        return PartyMemberDto.builder()
                .id(member.getId())
                .partyId(member.getParty().getId())
                .userId(member.getUser().getId())
                .userName(member.getUser().getUsername())
                .role(member.getRole())
                .invitedById(member.getInvitedBy() != null ? member.getInvitedBy().getId() : null)
                .invitedByName(member.getInvitedBy() != null ? member.getInvitedBy().getUsername() : null)
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
