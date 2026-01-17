package com.packt.blurApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.packt.blurApp.model.Party;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {
    Optional<Party> findByPartyDate(LocalDate partyDate);
    
    Optional<Party> findByPartyDateAndActiveTrue(LocalDate partyDate);
    
    boolean existsByPartyDate(LocalDate partyDate);

    boolean existsByPartyDateAndIdNot(LocalDate partyDate, Long id);
}