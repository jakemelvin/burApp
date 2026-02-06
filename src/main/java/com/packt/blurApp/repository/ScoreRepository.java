package com.packt.blurApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.packt.blurApp.model.Score;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    
    @EntityGraph(attributePaths = {"user", "submittedBy", "race"})
    List<Score> findByUserId(Long userId);
    
    @EntityGraph(attributePaths = {"user", "submittedBy", "race"})
    Optional<Score> findByRaceIdAndUserId(Long raceId, Long userId);
    
    @EntityGraph(attributePaths = {"user", "submittedBy", "race"})
    List<Score> findByRaceId(Long raceId);
    
    boolean existsByRaceIdAndUserId(Long raceId, Long userId);
    
    @Override
    @EntityGraph(attributePaths = {"user", "submittedBy", "race"})
    Optional<Score> findById(Long id);
}
