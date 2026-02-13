package com.packt.blurApp.dto.Party;

import com.packt.blurApp.model.enums.PartyRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePartyMemberRoleDto {
    @NotNull(message = "Role is required")
    private PartyRole role;
}
