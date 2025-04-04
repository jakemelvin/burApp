package com.packt.blurApp.dto.Party;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyResponseDto {
  private Long id;
  private LocalDateTime datePlayed;
}
