package com.packt.blurApp.dto.Party;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartyActiveStatusDto {
    private Long id;
    /** Party is not deactivated (soft delete flag). */
    private boolean active;
    /** Party actions are allowed (active + partyDate == today). */
    private boolean actionable;
    private LocalDate partyDate;
    private LocalDate today;
    private String reason;
}
