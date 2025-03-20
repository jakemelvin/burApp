package com.packt.blurApp.repository;

import com.packt.blurApp.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
}
