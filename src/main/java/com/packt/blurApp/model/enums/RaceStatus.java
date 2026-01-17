package com.packt.blurApp.model.enums;

public enum RaceStatus {
    PENDING,       // Race created but not started
    IN_PROGRESS,   // Race started, participants racing
    COMPLETED,     // Race finished, scores submitted
    CANCELLED      // Race cancelled
}
