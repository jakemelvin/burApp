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
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String userName;
  private String password;
  @OneToMany
  private Set<Permission> permissions = new HashSet<>();
  @ManyToMany
  @JoinTable(name = "user_race_participation", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "race_id"))
  private Set<Race> races = new HashSet<>();

  public void addRace(Race race) {
    this.races.add(race);
    race.getRacers().add(this);
  }

  public void removeRace(Race race) {
    this.races.remove(race);
    race.getRacers().remove(this);
  }
}
