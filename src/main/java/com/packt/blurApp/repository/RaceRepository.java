package com.packt.blurApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import com.packt.blurApp.model.Race;
import com.packt.blurApp.model.enums.RaceStatus;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {

    @EntityGraph(attributePaths = {
            "party",
            "creator",
            "scoreCollector",
            "card",
            "raceParameters",
            "participants",
            "scores",
            "scores.user",
            "attributions",
            "attributions.user",
            "attributions.car"
    })
    java.util.Optional<Race> findWithGraphById(Long id);

    @Override
    @EntityGraph(attributePaths = {
            "party",
            "creator",
            "scoreCollector",
            "card",
            "raceParameters",
            "participants",
            "scores",
            "scores.user",
            "attributions",
            "attributions.user",
            "attributions.car"
    })
    List<Race> findAll();

    @EntityGraph(attributePaths = {
            "party",
            "creator",
            "scoreCollector",
            "card",
            "raceParameters",
            "participants",
            "scores",
            "scores.user",
            "attributions",
            "attributions.user",
            "attributions.car"
    })
    List<Race> findByParty_Id(Long partyId);

    @EntityGraph(attributePaths = {
            "party",
            "creator",
            "scoreCollector",
            "card",
            "raceParameters",
            "participants",
            "scores",
            "scores.user",
            "attributions",
            "attributions.user",
            "attributions.car"
    })
    List<Race> findByStatus(RaceStatus status);

    List<Race> findByPartyIdAndStatus(Long partyId, RaceStatus status);
}
