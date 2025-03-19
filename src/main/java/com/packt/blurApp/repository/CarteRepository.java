package com.packt.blurApp.repository;

import com.packt.blurApp.model.Carte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarteRepository extends JpaRepository<Carte, Long> {
}
