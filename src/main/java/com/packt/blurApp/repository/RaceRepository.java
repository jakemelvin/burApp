package com.packt.blurApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packt.blurApp.model.Race;

public interface RaceRepository extends JpaRepository<Race, Long> {

  List<Race> findByParty_Id(Long partyId);

}
