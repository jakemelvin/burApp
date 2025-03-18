package com.packt.blurApp.service.race;

import org.springframework.stereotype.Service;

import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.Race;
import com.packt.blurApp.repository.RaceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RaceService implements IRaceService {
  private final RaceRepository raceRepository;

  @Override
  public Race getRaceById(Long id) {
    return raceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExceptions("Race not found!"));
  }

}
