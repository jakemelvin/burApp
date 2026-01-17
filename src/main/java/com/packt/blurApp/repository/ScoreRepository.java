package com.packt.blurApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.packt.blurApp.model.Score;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByUserId(Long userId);
    
    Optional<Score> findByRaceIdAndUserId(Long raceId, Long userId);
    
    List<Score> findByRaceId(Long raceId);
    
    boolean existsByRaceIdAndUserId(Long raceId, Long userId);
}
