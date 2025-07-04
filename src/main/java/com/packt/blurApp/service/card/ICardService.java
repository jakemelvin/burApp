package com.packt.blurApp.service.card;

import com.packt.blurApp.model.Card;
import com.packt.blurApp.model.Race;
import com.packt.blurApp.repository.CardRepository;
import com.packt.blurApp.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ICardService implements CardService {


    private final CardRepository cardRepository;
    private final RaceRepository raceRepository;

    public Card choisirCarteAleatoire(long raceId) {
        List<Card> cards = cardRepository.findAll();
        if (cards.isEmpty()) {
            throw new IllegalStateException("Aucune carte disponible.");
        }
        // Récupérer les cartes déjà attribuées aux courses actives
        List<Race> allRaces = raceRepository.findAll();
        Set<Long> usedCardIds = new HashSet<>();
        for (Race r : allRaces) {
            if (r.getCard() != null) {
                usedCardIds.add(r.getCard().getId());
            }
        }
        // Filtrer les cartes non utilisées
        List<Card> availableCards = new ArrayList<>();
        for (Card c : cards) {
            if (!usedCardIds.contains(c.getId())) {
                availableCards.add(c);
            }
        }
        Random random = new Random();
        Card card;
        if (!availableCards.isEmpty()) {
            card = availableCards.get(random.nextInt(availableCards.size()));
        } else {
            // Toutes les cartes sont déjà utilisées, on autorise les doublons
            card = cards.get(random.nextInt(cards.size()));
        }
        Race race = raceRepository.findById(raceId).orElseThrow(() -> new RuntimeException("Race not found"));
        race.setCard(card);
        raceRepository.save(race);
        return card;
    }

    //Lister toutes les cartes disponibles
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    // Convertir une Carte en DTO (Data Transfer Object) pour ne renvoyer que location et track
    public Map<String, String> toCarteDto(Card card) {
        Map<String, String> dto = new HashMap<>();
        dto.put("location", card.getLocation());
        dto.put("track", card.getTrack());
        dto.put("imageUrl", card.getImageUrl());
        return dto;
    }

    // Convertir une liste de Cartes en liste de DTOs
    public List<Map<String, String>> toCarteDtoList(List<Card> cards) {
        return cards.stream()
                .map(this::toCarteDto)
                .collect(Collectors.toList());
    }
}