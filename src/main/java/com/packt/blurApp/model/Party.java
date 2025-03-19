package com.packt.blurApp.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Party {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private LocalDate datePlayed;
  @JsonIgnore
  @OneToMany(mappedBy = "party")
  private Set<Race> racesPlayed = new HashSet<>();

  public void addRace(Race race) {
    racesPlayed.add(race);
    race.setParty(this);
  }

  public void removeRace(Race race) {
    racesPlayed.remove(race);
    race.setParty(null);
  }

  public void setDateToNow() {
    this.datePlayed = LocalDate.now();
  }
}
