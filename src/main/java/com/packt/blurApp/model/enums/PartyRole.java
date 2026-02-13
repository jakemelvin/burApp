package com.packt.blurApp.model.enums;

/**
 * Defines the roles a user can have within a party.
 * 
 * HOST: The creator/owner of the party. Full control.
 * CO_HOST: Delegated manager. Can manage races, scores, participants.
 * PARTICIPANT: Regular player. Can only participate in races.
 */
public enum PartyRole {
    HOST,       // Creator - full control, can assign co-hosts, delete party
    CO_HOST,    // Delegated manager - can manage races, scores, add participants
    PARTICIPANT // Regular player - can only participate
}
