package com.packt.blurApp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.packt.blurApp.model.enums.PartyRole;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"creator", "partyMembers", "races"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "party")
public class Party {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    // nullable=true to allow migration on existing DB rows
    @Column(nullable = true, unique = true)
    private LocalDate partyDate;
    
    @Column(nullable = true)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = true)
    private User creator;
    
    // New: PartyMember relationship for role-based management
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PartyMember> partyMembers = new HashSet<>();
    
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Builder.Default
    private Set<Race> races = new HashSet<>();
    
    @Column(nullable = true)
    @Builder.Default
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (partyDate == null) {
            partyDate = LocalDate.now();
        }
    }

    // Helper methods for races
    public void addRace(Race race) {
        races.add(race);
        race.setParty(this);
    }

    public void removeRace(Race race) {
        races.remove(race);
        race.setParty(null);
    }
    
    // Helper methods for party members
    public void addPartyMember(PartyMember partyMember) {
        partyMembers.add(partyMember);
        partyMember.setParty(this);
    }
    
    public void removePartyMember(PartyMember partyMember) {
        partyMembers.remove(partyMember);
        partyMember.setParty(null);
    }
    
    // Get all members (users) regardless of role
    public Set<User> getMembers() {
        return partyMembers.stream()
                .map(PartyMember::getUser)
                .collect(Collectors.toSet());
    }
    
    // Get host (creator)
    public User getHost() {
        return partyMembers.stream()
                .filter(PartyMember::isHost)
                .map(PartyMember::getUser)
                .findFirst()
                .orElse(creator); // Fallback to legacy creator field
    }
    
    // Get all co-hosts
    public Set<User> getCoHosts() {
        return partyMembers.stream()
                .filter(PartyMember::isCoHost)
                .map(PartyMember::getUser)
                .collect(Collectors.toSet());
    }
    
    // Get all managers (host + co-hosts)
    public Set<User> getManagers() {
        return partyMembers.stream()
                .filter(PartyMember::canManageParty)
                .map(PartyMember::getUser)
                .collect(Collectors.toSet());
    }
    
    // Get participants only (non-managers)
    public Set<User> getParticipants() {
        return partyMembers.stream()
                .filter(PartyMember::isParticipant)
                .map(PartyMember::getUser)
                .collect(Collectors.toSet());
    }
    
    // Check if user is a member of the party
    public boolean isMember(User user) {
        return partyMembers.stream()
                .anyMatch(pm -> pm.getUser().equals(user));
    }
    
    // Check if user can manage the party (host or co-host)
    public boolean canManage(User user) {
        // GREAT_ADMIN can always manage
        if (user.getRole() != null && "GREAT_ADMIN".equals(user.getRole().getName())) {
            return true;
        }
        return partyMembers.stream()
                .anyMatch(pm -> pm.getUser().equals(user) && pm.canManageParty());
    }
    
    // Check if user is the host
    public boolean isHost(User user) {
        return partyMembers.stream()
                .anyMatch(pm -> pm.getUser().equals(user) && pm.isHost());
    }
    
    // Check if user is a co-host
    public boolean isCoHost(User user) {
        return partyMembers.stream()
                .anyMatch(pm -> pm.getUser().equals(user) && pm.isCoHost());
    }
    
    // Get the PartyMember for a specific user
    public PartyMember getPartyMember(User user) {
        return partyMembers.stream()
                .filter(pm -> pm.getUser().equals(user))
                .findFirst()
                .orElse(null);
    }

    public boolean isActive() {
        return active == null ? true : active;
    }
}
