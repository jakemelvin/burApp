package com.packt.blurApp.dto.Party;

import com.packt.blurApp.model.enums.PartyRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyMemberDto {
    private Long id;
    private Long partyId;
    private Long userId;
    private String userName;
    private PartyRole role;
    private Long invitedById;
    private String invitedByName;
    private LocalDateTime joinedAt;
}
