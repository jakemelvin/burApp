package com.packt.blurApp.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.packt.blurApp.model.enums.AttributionType;
import com.packt.blurApp.model.enums.RaceStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"party", "creator", "scoreCollector", "participants", "scores", "attributions", "card", "raceParameters"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "race")
public class Race {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @NotNull
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime startedAt;
    
    @Column
    private LocalDateTime completedAt;
    
    // nullable=true to allow migration on existing DB rows
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    @Builder.Default
    private RaceStatus status = RaceStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    @Builder.Default
    private AttributionType attributionType = AttributionType.PER_USER;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = true)
    private Party party;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = true)
    private User creator;
    
    // Score collector: user assigned to collect scores at the end
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_collector_id")
    private User scoreCollector;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "race_participants",
        joinColumns = @JoinColumn(name = "race_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> participants = new HashSet<>();
    
    @OneToMany(mappedBy = "race", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Score> scores = new HashSet<>();
    
    @OneToMany(mappedBy = "race", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Attribution> attributions = new HashSet<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "race_race_parameters",
        joinColumns = @JoinColumn(name = "race_id"),
        inverseJoinColumns = @JoinColumn(name = "parameter_id")
    )
    @Builder.Default
    private Set<RaceParameters> raceParameters = new HashSet<>();
    
    @Column
    private String favoriteCard;
    
    @Column
    private Integer confidencePoints;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Helper methods
    public void addParticipant(User user) {
        participants.add(user);
        user.addRace(this);
    }

    public void removeParticipant(User user) {
        participants.remove(user);
        user.removeRace(this);
    }
    
    public void addRaceParameter(RaceParameters raceParameter) {
        raceParameters.add(raceParameter);
    }

    public void removeRaceParameter(RaceParameters raceParameter) {
        raceParameters.remove(raceParameter);
    }
    
    public void addScore(Score score) {
        scores.add(score);
        score.setRace(this);
    }
    
    public void addAttribution(Attribution attribution) {
        attributions.add(attribution);
        attribution.setRace(this);
    }
    
    public boolean isParticipant(User user) {
        return participants.contains(user);
    }
    
    public void start() {
        this.status = RaceStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }
    
    public void complete() {
        this.status = RaceStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = RaceStatus.CANCELLED;
    }
}
