package com.packt.blurApp.dto.RaceParameters;

import lombok.Data;

@Data
public class AddRaceParametersDto {
  Long id;
  String name;
  Boolean isActive=false;
  Boolean isChecked;
}
