package com.packt.blurApp.controller;

import com.packt.blurApp.model.Voiture;
import com.packt.blurApp.service.VoitureServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/voitures")
public class VoitureController {

    @Autowired
    private VoitureServiceImpl voitureService;


    // Mode 1 : Même voiture pour tous les joueurs
    @GetMapping("/attribution-globale")
    public Voiture attribuerVoitureGlobale() {

        return voitureService.attribuerVoitureAleatoire();
    }

    // Mode 2 : Voiture aléatoire par joueur (doublons possibles)
    @GetMapping("/attribution-individuelle")
    public Map<String, Object> attribuerVoituresIndividuelles(@RequestBody List<String> joueurs) {
        int nombreJoueurs = joueurs.size();
        List<Voiture> voitures = voitureService.attribuerVoituresParJoueur(nombreJoueurs);

        // Associer chaque joueur à une voiture
        Map<String, Voiture> attribution = new LinkedHashMap<>();
        for (int i = 0; i < joueurs.size(); i++) {
            attribution.put(joueurs.get(i), voitures.get(i));
        }

        // Structure de réponse
        Map<String, Object> response = new HashMap<>();
        response.put("attributions", attribution);

        return response;
    }
}