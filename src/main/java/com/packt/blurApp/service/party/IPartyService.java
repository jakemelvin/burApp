package com.packt.blurApp.service.party;

import java.util.List;

import com.packt.blurApp.dto.Party.PartyUpdateDto;
import com.packt.blurApp.model.Party;

public interface IPartyService {
  Party createParty();

  Party getPartyById(Long id);

  List<Party> getAllParties();

  Party updateParty(PartyUpdateDto updatedParty, Long partyId);
}
