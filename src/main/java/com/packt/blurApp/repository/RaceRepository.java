package com.packt.blurApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packt.blurApp.model.Race;

public interface RaceRepository extends JpaRepository<Race, Long> {

}
