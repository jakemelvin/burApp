package com.packt.blurApp.controller;

import com.packt.blurApp.model.Card;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.card.ICardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/cards")
public class CardController {
    private final ICardService cardService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_MAPS')")
    public ResponseEntity<ApiResponse<?>> getAllCards() {
        return ResponseEntity.ok(ApiResponse.success("All cards fetched", cardService.getAllCards()));
    }
}
