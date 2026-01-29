package com.packt.blurApp.mapper.partyMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.packt.blurApp.dto.Party.PartyGetResponseDto;
import com.packt.blurApp.dto.Party.PartyResponseDto;
import com.packt.blurApp.mapper.raceMapper.RaceMapper;
import com.packt.blurApp.model.Party;

public class PartyMapper {
    
    public static PartyResponseDto toPartyResponseDto(Party party) {
        PartyResponseDto dto = new PartyResponseDto();
        dto.setId(party.getId());
        dto.setDatePlayed(party.getPartyDate() != null ? party.getPartyDate().atStartOfDay() : null);
        return dto;
    }

    public static PartyGetResponseDto toPartyGetResponseDto(Party party) {
        PartyGetResponseDto dto = new PartyGetResponseDto();
        dto.setId(party.getId());
        dto.setDatePlayed(party.getPartyDate() != null ? party.getPartyDate().atStartOfDay() : null);

        if (party.getCreator() != null) {
            dto.setCreator(new com.packt.blurApp.dto.Party.PartyUserMiniDto(
                party.getCreator().getId(),
                party.getCreator().getUsername()
            ));
        }

        if (party.getManagers() != null && org.hibernate.Hibernate.isInitialized(party.getManagers())) {
            java.util.Set<com.packt.blurApp.dto.Party.PartyUserMiniDto> managerDtos = party.getManagers().stream()
                .map(u -> new com.packt.blurApp.dto.Party.PartyUserMiniDto(u.getId(), u.getUsername()))
                .collect(Collectors.toSet());
            dto.setManagers(managerDtos);
        }

        return dto;
    }

    public static Set<PartyResponseDto> toPartyResponseDtoList(List<Party> parties) {
        Set<PartyResponseDto> dtoList = new HashSet<>();
        parties.forEach(party -> {
            dtoList.add(toPartyResponseDto(party));
        });
        return dtoList;
    }

    public static Set<PartyGetResponseDto> toPartyGetResponseDtoList(List<Party> parties) {
        Set<PartyGetResponseDto> dtoList = new HashSet<>();
        parties.forEach(party -> {
            dtoList.add(toPartyGetResponseDto(party));
        });
        return dtoList;
    }
}
