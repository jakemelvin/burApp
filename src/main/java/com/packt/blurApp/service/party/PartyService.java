package com.packt.blurApp.service.party;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packt.blurApp.exceptions.BadRequestException;
import com.packt.blurApp.exceptions.ConflictException;
import com.packt.blurApp.exceptions.ForbiddenException;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.Party;
import com.packt.blurApp.model.User;
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
                    
                    // Creator automatically becomes a member and manager
                    newParty.addMember(currentUser);
                    newParty.addManager(currentUser);
                    
                    Party savedParty = partyRepository.save(newParty);
                    log.info("Party created successfully for date: {} by user: {}", today, currentUser.getUsername());
                    
                    return savedParty;
                });
        
        // Initialize lazy collections to avoid LazyInitializationException
        org.hibernate.Hibernate.initialize(party.getRaces());
        org.hibernate.Hibernate.initialize(party.getMembers());
        org.hibernate.Hibernate.initialize(party.getManagers());
        
        return party;
    }

    @Override
    @Transactional(readOnly = true)
    public Party getPartyById(Long id) {
        log.debug("Fetching party by ID: {}", id);
        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExceptions("Party not found with ID: " + id));
        
        // Initialize lazy collections to avoid LazyInitializationException
        org.hibernate.Hibernate.initialize(party.getRaces());
        org.hibernate.Hibernate.initialize(party.getMembers());
        org.hibernate.Hibernate.initialize(party.getManagers());
        
        return party;
    }

    @Override
    @Transactional(readOnly = true)
    public Party getPartyByDate(LocalDate date) {
        log.debug("Fetching party by date: {}", date);
        Party party = partyRepository.findByPartyDate(date)
                .orElseThrow(() -> new ResourceNotFoundExceptions("No party found for date: " + date));
        
        // Initialize lazy collections to avoid LazyInitializationException
        org.hibernate.Hibernate.initialize(party.getRaces());
        org.hibernate.Hibernate.initialize(party.getMembers());
        org.hibernate.Hibernate.initialize(party.getManagers());
        
        return party;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Party> getAllParties() {
        log.debug("Fetching all parties");
        List<Party> parties = partyRepository.findAll();
        
        // Initialize lazy collections for each party
        parties.forEach(party -> {
            org.hibernate.Hibernate.initialize(party.getRaces());
            org.hibernate.Hibernate.initialize(party.getMembers());
            org.hibernate.Hibernate.initialize(party.getManagers());
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
        
        if (party.isMember(user)) {
            throw new ConflictException("User is already a member of this party");
        }
        
        party.addMember(user);
        Party updatedParty = partyRepository.save(party);
        
        log.info("User {} joined party {} successfully", user.getUsername(), partyId);
        return updatedParty;
    }

    @Override
    @Transactional
    public Party leaveParty(Long partyId, User user) {
        log.info("User {} leaving party {}", user.getUsername(), partyId);
        
        Party party = getPartyById(partyId);
        
        if (!party.isMember(user)) {
            throw new BadRequestException("User is not a member of this party");
        }
        
        // Creator cannot leave their own party
        if (party.getCreator().equals(user)) {
            throw new ForbiddenException("Party creator cannot leave the party");
        }
        
        party.removeMember(user);
        
        // Also remove from managers if they were one
        if (party.isManager(user)) {
            party.removeManager(user);
        }
        
        Party updatedParty = partyRepository.save(party);
        
        log.info("User {} left party {} successfully", user.getUsername(), partyId);
        return updatedParty;
    }

    @Override
    @Transactional
    public Party assignManager(Long partyId, Long userId) {
        log.info("Assigning user {} as manager of party {}", userId, partyId);
        
        User currentUser = userService.getCurrentUser();
        Party party = getPartyById(partyId);
        
        // Only creator or existing managers can assign new managers
        if (!party.isManager(currentUser)) {
            throw new ForbiddenException("Only party managers can assign new managers");
        }
        
        User userToPromote = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundExceptions("User not found with ID: " + userId));
        
        if (!party.isMember(userToPromote)) {
            throw new BadRequestException("User must be a party member to become a manager");
        }
        
        if (party.isManager(userToPromote)) {
            throw new ConflictException("User is already a manager of this party");
        }
        
        party.addManager(userToPromote);
        Party updatedParty = partyRepository.save(party);
        
        log.info("User {} assigned as manager of party {}", userId, partyId);
        return updatedParty;
    }

    @Override
    @Transactional
    public Party removeManager(Long partyId, Long userId) {
        log.info("Removing user {} as manager of party {}", userId, partyId);
        
        User currentUser = userService.getCurrentUser();
        Party party = getPartyById(partyId);
        
        // Only creator can remove managers
        if (!party.getCreator().equals(currentUser)) {
            throw new ForbiddenException("Only party creator can remove managers");
        }
        
        User userToDemote = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundExceptions("User not found with ID: " + userId));
        
        // Cannot remove creator as manager
        if (party.getCreator().equals(userToDemote)) {
            throw new BadRequestException("Cannot remove creator as manager");
        }
        
        if (!party.isManager(userToDemote)) {
            throw new BadRequestException("User is not a manager of this party");
        }
        
        party.removeManager(userToDemote);
        Party updatedParty = partyRepository.save(party);
        
        log.info("User {} removed as manager of party {}", userId, partyId);
        return updatedParty;
    }

    @Override
    @Transactional
    public void deactivateParty(Long partyId) {
        log.info("Deactivating party {}", partyId);
        
        User currentUser = userService.getCurrentUser();
        Party party = getPartyById(partyId);
        
        // Only creator can deactivate party
        if (!party.getCreator().equals(currentUser)) {
            throw new ForbiddenException("Only party creator can deactivate the party");
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
    public java.util.Set<User> getPartyMembers(Long partyId) {
        Party party = getPartyById(partyId);
        org.hibernate.Hibernate.initialize(party.getMembers());
        return party.getMembers();
    }
}
