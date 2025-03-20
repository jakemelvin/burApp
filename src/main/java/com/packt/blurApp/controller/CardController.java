package com.packt.blurApp.controller;

import com.packt.blurApp.model.Card;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.card.ICardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/cards")
public class CardController {

    private final ICardService carteService;

    @GetMapping("/random")
    public ResponseEntity<ApiResponse> chooseRandomCard() {
        Card card = carteService.choisirCarteAleatoire();
        return ResponseEntity.ok(new ApiResponse("Random card choosed", card)); // Retourne la carte choisie
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllCartes() {
        return ResponseEntity.ok(new ApiResponse("All cards fetched", carteService.getAllCards()));

    }

}