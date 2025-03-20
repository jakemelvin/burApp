package com.packt.blurApp.model;

import java.util.HashSet;
import java.util.Set;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
  @OneToMany(mappedBy = "race")
  private Set<Score> scores = new HashSet<>();
  @ManyToMany(mappedBy = "races")
  private Set<User> racers = new HashSet<>();
  @ManyToOne
  @JoinColumn(name = "party_id")
  private Party party;
  @ManyToMany
  @JoinTable(name = "race_race_parameters", joinColumns = @JoinColumn(name = "race_id"), inverseJoinColumns = @JoinColumn(name = "race_parameters_id"))
  private Set<RaceParameters> raceParameters = new HashSet<>();

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
