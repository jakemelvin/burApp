package com.packt.blurApp.repository;

import com.packt.blurApp.model.Party;
import com.packt.blurApp.model.PartyMember;
import com.packt.blurApp.model.User;
import com.packt.blurApp.model.enums.PartyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {

    // Find by party and user
    Optional<PartyMember> findByPartyAndUser(Party party, User user);
    
    Optional<PartyMember> findByPartyIdAndUserId(Long partyId, Long userId);
    
    // Check if user is a member of a party
    boolean existsByPartyAndUser(Party party, User user);
    
    boolean existsByPartyIdAndUserId(Long partyId, Long userId);
    
    // Find all members of a party
    List<PartyMember> findByParty(Party party);
    
    List<PartyMember> findByPartyId(Long partyId);
    
    // Find all parties for a user
    List<PartyMember> findByUser(User user);
    
    List<PartyMember> findByUserId(Long userId);
    
    // Find members by role
    List<PartyMember> findByPartyAndRole(Party party, PartyRole role);
    
    List<PartyMember> findByPartyIdAndRole(Long partyId, PartyRole role);
    
    // Find the host of a party
    @Query("SELECT pm FROM PartyMember pm WHERE pm.party.id = :partyId AND pm.role = 'HOST'")
    Optional<PartyMember> findHostByPartyId(@Param("partyId") Long partyId);
    
    // Find all co-hosts of a party
    @Query("SELECT pm FROM PartyMember pm WHERE pm.party.id = :partyId AND pm.role = 'CO_HOST'")
    List<PartyMember> findCoHostsByPartyId(@Param("partyId") Long partyId);
    
    // Find all managers (host + co-hosts)
    @Query("SELECT pm FROM PartyMember pm WHERE pm.party.id = :partyId AND pm.role IN ('HOST', 'CO_HOST')")
    List<PartyMember> findManagersByPartyId(@Param("partyId") Long partyId);
    
    // Check if user can manage party
    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END FROM PartyMember pm " +
           "WHERE pm.party.id = :partyId AND pm.user.id = :userId AND pm.role IN ('HOST', 'CO_HOST')")
    boolean canUserManageParty(@Param("partyId") Long partyId, @Param("userId") Long userId);
    
    // Check if user is host
    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END FROM PartyMember pm " +
           "WHERE pm.party.id = :partyId AND pm.user.id = :userId AND pm.role = 'HOST'")
    boolean isUserHostOfParty(@Param("partyId") Long partyId, @Param("userId") Long userId);
    
    // Delete member from party
    void deleteByPartyAndUser(Party party, User user);
    
    void deleteByPartyIdAndUserId(Long partyId, Long userId);
    
    // Count members by party
    long countByPartyId(Long partyId);
    
    // Count by role
    long countByPartyIdAndRole(Long partyId, PartyRole role);
}
