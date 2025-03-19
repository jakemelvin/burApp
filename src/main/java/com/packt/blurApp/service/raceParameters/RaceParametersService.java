package com.packt.blurApp.service.raceParameters;

import org.springframework.stereotype.Service;

import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.RaceParameters;
import com.packt.blurApp.repository.RaceParametersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RaceParametersService implements IRaceParametersService {
  private final RaceParametersRepository raceParametersRepository;

  @Override
  public RaceParameters getRaceParameterById(Long id) {
    return raceParametersRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundExceptions("Race Parameters not found!"));
  
      }

}
