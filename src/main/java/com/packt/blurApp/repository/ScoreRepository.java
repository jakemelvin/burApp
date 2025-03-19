package com.packt.blurApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packt.blurApp.model.Score;

public interface ScoreRepository extends JpaRepository<Score, Long> {

}
