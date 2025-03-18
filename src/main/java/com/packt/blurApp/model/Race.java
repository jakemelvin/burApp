package com.packt.blurApp.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
  private Set<Score> scores;
  @OneToMany(mappedBy = "race", cascade = CascadeType.ALL)
  private Set<User> racers;
  @ManyToOne
  @JsonIgnore
  @JoinColumn(name = "party_id")
  private Party party;
  @OneToMany(mappedBy = "race")
  private Set<RaceParameters> raceParameters;
}
