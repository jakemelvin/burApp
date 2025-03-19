package com.packt.blurApp.service;

import com.packt.blurApp.model.Carte;
import com.packt.blurApp.repository.CarteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CarteServiceImpl implements CarteService {

    @Autowired
    private CarteRepository carteRepository;

    public Carte choisirCarteAleatoire() {
        List<Carte> cartes = carteRepository.findAll();
        if (cartes.isEmpty()) {
            throw new IllegalStateException("Aucune carte disponible.");
        }
        Random random = new Random();
        return cartes.get(random.nextInt(cartes.size()));
    }

    //Lister toutes les cartes disponibles
    public List<Carte> ListerCartes() {
        return carteRepository.findAll();
    }

    // Convertir une Carte en DTO (Data Transfer Object) pour ne renvoyer que location et track
    public Map<String, String> toCarteDto(Carte carte) {
        Map<String, String> dto = new HashMap<>();
        dto.put("location", carte.getLocation());
        dto.put("track", carte.getTrack());
        dto.put("imageUrl", carte.getImageUrl());
        return dto;
    }

    // Convertir une liste de Cartes en liste de DTOs
    public List<Map<String, String>> toCarteDtoList(List<Carte> cartes) {
        return cartes.stream()
                .map(this::toCarteDto)
                .collect(Collectors.toList());
    }
}