package com.packt.blurApp.mapper.partyMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.packt.blurApp.dto.Party.PartyGetResponseDto;
import com.packt.blurApp.dto.Party.PartyMemberDto;
import com.packt.blurApp.dto.Party.PartyResponseDto;
import com.packt.blurApp.dto.Party.PartyUserMiniDto;
import com.packt.blurApp.model.Party;
import com.packt.blurApp.model.PartyMember;
import com.packt.blurApp.model.User;

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
        dto.setActive(party.isActive());

        // Legacy creator field
        if (party.getCreator() != null) {
            dto.setCreator(new PartyUserMiniDto(
                party.getCreator().getId(),
                party.getCreator().getUsername()
            ));
        }

        // Map party members if initialized
        if (party.getPartyMembers() != null && org.hibernate.Hibernate.isInitialized(party.getPartyMembers())) {
            Set<PartyMember> partyMembers = party.getPartyMembers();
            
            // Host
            User host = party.getHost();
            if (host != null) {
                dto.setHost(new PartyUserMiniDto(host.getId(), host.getUsername()));
            }
            
            // Co-hosts
            Set<PartyUserMiniDto> coHostDtos = party.getCoHosts().stream()
                .map(u -> new PartyUserMiniDto(u.getId(), u.getUsername()))
                .collect(Collectors.toSet());
            dto.setCoHosts(coHostDtos);
            
            // Participants (non-managers)
            Set<PartyUserMiniDto> participantDtos = party.getParticipants().stream()
                .map(u -> new PartyUserMiniDto(u.getId(), u.getUsername()))
                .collect(Collectors.toSet());
            dto.setParticipants(participantDtos);
            
            // All managers (host + co-hosts) for legacy compatibility
            Set<PartyUserMiniDto> managerDtos = party.getManagers().stream()
                .map(u -> new PartyUserMiniDto(u.getId(), u.getUsername()))
                .collect(Collectors.toSet());
            dto.setManagers(managerDtos);
            
            // Detailed members list with roles
            List<PartyMemberDto> memberDtos = partyMembers.stream()
                .map(PartyMapper::toPartyMemberDto)
                .collect(Collectors.toList());
            dto.setMembers(memberDtos);
        }

        return dto;
    }
    
    public static PartyMemberDto toPartyMemberDto(PartyMember member) {
        return PartyMemberDto.builder()
                .id(member.getId())
                .partyId(member.getParty().getId())
                .userId(member.getUser().getId())
                .userName(member.getUser().getUsername())
                .role(member.getRole())
                .invitedById(member.getInvitedBy() != null ? member.getInvitedBy().getId() : null)
                .invitedByName(member.getInvitedBy() != null ? member.getInvitedBy().getUsername() : null)
                .joinedAt(member.getJoinedAt())
                .build();
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
