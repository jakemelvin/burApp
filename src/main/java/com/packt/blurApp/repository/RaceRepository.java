package com.packt.blurApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.packt.blurApp.model.Race;
import com.packt.blurApp.model.enums.RaceStatus;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {
    List<Race> findByParty_Id(Long partyId);
    
    List<Race> findByPartyIdAndStatus(Long partyId, RaceStatus status);
    
    List<Race> findByStatus(RaceStatus status);
}
