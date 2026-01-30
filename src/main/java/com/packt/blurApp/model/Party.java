package com.packt.blurApp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@ToString(exclude = {"creator", "members", "managers", "races"})
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
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "party_members",
        joinColumns = @JoinColumn(name = "party_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> members = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "party_managers",
        joinColumns = @JoinColumn(name = "party_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> managers = new HashSet<>();
    
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

    // Helper methods
    public void addRace(Race race) {
        races.add(race);
        race.setParty(this);
    }

    public void removeRace(Race race) {
        races.remove(race);
        race.setParty(null);
    }
    
    public void addMember(User user) {
        members.add(user);
    }
    
    public void removeMember(User user) {
        members.remove(user);
    }
    
    public void addManager(User user) {
        managers.add(user);
    }
    
    public void removeManager(User user) {
        managers.remove(user);
    }
    
    public boolean isMember(User user) {
        return members.contains(user);
    }
    
    public boolean isManager(User user) {
        return managers.contains(user) || (creator != null && creator.equals(user));
    }

    public boolean isActive() {
        return active == null ? true : active;
    }
}
