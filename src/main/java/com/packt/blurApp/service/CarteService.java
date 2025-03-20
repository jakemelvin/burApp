package com.packt.blurApp.service;

import com.packt.blurApp.model.Carte;

import java.util.List;
import java.util.Map;

public interface CarteService {
    Carte choisirCarteAleatoire();
    List<Carte> ListerCartes();
    Map<String, String> toCarteDto(Carte carte);
    List<Map<String, String>> toCarteDtoList(List<Carte> cartes);

}
