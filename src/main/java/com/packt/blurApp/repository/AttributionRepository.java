package com.packt.blurApp.repository;

import com.packt.blurApp.model.Attribution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributionRepository extends JpaRepository<Attribution, Long> {
}
