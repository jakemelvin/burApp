package com.packt.blurApp.dto.Party;

import com.packt.blurApp.model.enums.PartyRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPartyMemberDto {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private PartyRole role = PartyRole.PARTICIPANT;
}
