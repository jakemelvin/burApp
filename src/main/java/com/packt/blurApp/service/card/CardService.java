package com.packt.blurApp.service.card;

import com.packt.blurApp.model.Card;

import java.util.List;
import java.util.Map;

public interface CardService {
    Card choisirCarteAleatoire();
    List<Card> getAllCards();
    Map<String, String> toCarteDto(Card card);
    List<Map<String, String>> toCarteDtoList(List<Card> cards);

}
