package com.packt.blurApp.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Race {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private LocalDateTime createdAt;
  @OneToMany(mappedBy = "race", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Score> scores = new HashSet<>();
  @ManyToMany(mappedBy = "races")
  private Set<User> racers = new HashSet<>();
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "party_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private Party party;
  @ManyToMany
  @JoinTable(name = "race_race_parameters", joinColumns = @JoinColumn(name = "race_id"), inverseJoinColumns = @JoinColumn(name = "race_parameters_id"))
  private Set<RaceParameters> raceParameters = new HashSet<>();

  @OneToMany(mappedBy = "race")
  private Set<Attribution> attributions = new HashSet<>();

  @OneToOne
  @JoinColumn(name = "card_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private Card card;

  @OneToOne
  @JoinColumn(name = "car_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private Car car;

  public void addRaceParameter(RaceParameters raceParameter) {
    raceParameters.add(raceParameter);
  }

  public void removeRaceParameter(RaceParameters raceParameter) {
    raceParameters.remove(raceParameter);
  }

  public void addRacers(User racer) {
    racers.add(racer);
    racer.addRace(this);
  }

  public void removeRacers(User racer) {
    racers.remove(racer);
    racer.removeRace(this);
  }
}
