package com.packt.blurApp.dto.Party;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddPartyDto {
  private LocalDate datePlayed;
}
