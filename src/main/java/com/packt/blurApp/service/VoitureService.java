package com.packt.blurApp.service;

import com.packt.blurApp.model.Voiture;

import java.util.List;

public interface VoitureService{
    Voiture attribuerVoitureAleatoire();
    List<Voiture> attribuerVoituresParJoueur(int nombreJoueurs);
}
