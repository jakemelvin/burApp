package com.packt.blurApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packt.blurApp.model.RaceParameters;

public interface RaceParametersRepository extends JpaRepository<RaceParameters, Long> {

}
