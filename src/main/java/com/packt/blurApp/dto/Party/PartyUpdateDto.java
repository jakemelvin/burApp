package com.packt.blurApp.dto.Party;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartyUpdateDto {
  private List<Long> raceIds;
}
