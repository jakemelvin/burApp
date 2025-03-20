package com.packt.blurApp.service;

import com.packt.blurApp.model.Voiture;
import com.packt.blurApp.repository.VoitureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VoitureServiceImpl implements VoitureService {

    @Autowired
    private VoitureRepository voitureRepository;

    // Mode 1 : Une voiture aléatoire pour tous les joueurs
    public Voiture attribuerVoitureAleatoire() {
        List<Voiture> voitures = voitureRepository.findAll();
        if (voitures.isEmpty()) {
            throw new IllegalStateException("Aucune voiture disponible.");
        }
        return voitures.get(new Random().nextInt(voitures.size()));
    }

    // Mode 2 : Attribuer une voiture aléatoire à chaque joueur (pas d'unicité requise)
    public List<Voiture> attribuerVoituresParJoueur(int nombreJoueurs) {
        List<Voiture> voitures = voitureRepository.findAll();
        if (voitures.isEmpty()) {
            throw new IllegalStateException("Aucune voiture disponible.");
        }

        List<Voiture> attributions = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < nombreJoueurs; i++) {
            // Choisir une voiture aléatoire (peut être la même pour plusieurs joueurs)
            Voiture voiture = voitures.get(random.nextInt(voitures.size()));
            attributions.add(voiture);
        }

        return attributions;
    }
}