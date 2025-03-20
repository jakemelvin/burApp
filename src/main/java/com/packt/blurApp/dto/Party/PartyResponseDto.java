package com.packt.blurApp.dto.Party;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyResponseDto {
  private Long id;
  private LocalDate datePlayed;
}
