package com.packt.blurApp.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.CascadeType;
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
  private LocalDateTime datePlayed;
  @OneToMany(mappedBy = "party", cascade = { CascadeType.ALL }, orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.CASCADE)
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
    this.datePlayed = LocalDateTime.now();
  }
}
