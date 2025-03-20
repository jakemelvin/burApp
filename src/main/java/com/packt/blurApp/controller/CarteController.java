package com.packt.blurApp.controller;

import com.packt.blurApp.model.Carte;
import com.packt.blurApp.service.CarteServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/cartes")
public class CarteController {

    @Autowired
    private CarteServiceImpl carteService;

    @GetMapping("/aleatoire")
    public ResponseEntity<?> choisirCarteAleatoire() {
        try {
            Carte carte = carteService.choisirCarteAleatoire();
            return ResponseEntity.ok(carte); // Retourne la carte choisie
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Aucune carte disponible.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse); // Message d'erreur
        }
    }

}