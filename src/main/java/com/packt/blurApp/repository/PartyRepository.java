package com.packt.blurApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packt.blurApp.model.Party;

public interface PartyRepository extends JpaRepository<Party, Long> {
}