package com.packt.blurApp.service.party;

import java.time.LocalDate;
import java.util.List;

import com.packt.blurApp.model.Party;
import com.packt.blurApp.model.User;

public interface IPartyService {
    Party getTodayPartyOrCreate();
    
    Party getPartyById(Long id);
    
    Party getPartyByDate(LocalDate date);
    
    List<Party> getAllParties();
    
    Party joinParty(Long partyId, User user);
    
    Party leaveParty(Long partyId, User user);
    
    Party assignManager(Long partyId, Long userId);
    
    Party removeManager(Long partyId, Long userId);
    
    void deactivateParty(Long partyId);
}
