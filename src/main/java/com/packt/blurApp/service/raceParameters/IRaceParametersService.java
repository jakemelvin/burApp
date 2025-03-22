package com.packt.blurApp.service.raceParameters;

import java.util.List;

import com.packt.blurApp.model.RaceParameters;

public interface IRaceParametersService {
  RaceParameters getRaceParameterById(Long id);

  List<RaceParameters> getAllRaceParameters();
}
