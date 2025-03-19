package com.packt.blurApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packt.blurApp.model.Score;

public interface ScoreRepository extends JpaRepository<Score, Long> {

  List<Score> findByUser_Id(Long userId);

  Score findByRace_IdAndUser_Id(Long raceId, Long userId);

	List<Score> findByRace_Id(Long raceId);

}
