package com.packt.blurApp.model;

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
@NoArgsConstructor
@AllArgsConstructor
@Table(name="app_user")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String userName;
  private String password;
  @OneToMany
  private Set<Permission> permissions = new HashSet<>();
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Score> scores = new HashSet<>();
  @ManyToMany
  @JoinTable(name = "user_race_participation", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "race_id"))
  private Set<Race> races = new HashSet<>();

  public User(String userName, String password) {
    this.userName = userName;
    this.password = password;
  }

  public void addRace(Race race) {
    this.races.add(race);
    race.getRacers().add(this);
  }

  public void removeRace(Race race) {
    this.races.remove(race);
    race.getRacers().remove(this);
  }
}
