package com.packt.blurApp.service.score;

import java.util.List;

import org.springframework.stereotype.Service;

import com.packt.blurApp.dto.Score.AddScoreDto;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.Race;
import com.packt.blurApp.model.Score;
import com.packt.blurApp.repository.RaceRepository;
import com.packt.blurApp.repository.ScoreRepository;
import com.packt.blurApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScoreService implements IScoreService {
  private final ScoreRepository scoreRepository;
  private final UserRepository userRepository;
  private final RaceRepository raceRepository;

  @Override
  public Score addScore(AddScoreDto addScoreDto) {
    try {
      Score score = new Score();
      Race playedRace = raceRepository.findById(addScoreDto.getRaceId())
          .orElseThrow(() -> new ResourceNotFoundExceptions("Race not found"));
      score.setUser(userRepository.findById(addScoreDto.getUserId())
          .orElseThrow(() -> new ResourceNotFoundExceptions("User not found")));
      score.setRace(playedRace);
      score.setValue(addScoreDto.getValue());
      return scoreRepository.save(score);
    } catch (ResourceNotFoundExceptions e) {
      throw new ResourceNotFoundExceptions(e.getMessage());
    }
  }

  @Override
  public Score updateScore(AddScoreDto updateScoreDto, Long scoreId) {
    Score scoreToUpdate = getScoreById(scoreId);
    Race playedRace = raceRepository.findById(updateScoreDto.getRaceId())
        .orElseThrow(() -> new ResourceNotFoundExceptions("Race not found"));
    int scoreValue = playedRace.getRacers().size() - updateScoreDto.getValue() + 1;
    scoreToUpdate.setValue(scoreValue);
    scoreToUpdate.setUser(userRepository.findById(updateScoreDto.getUserId())
        .orElseThrow(() -> new ResourceNotFoundExceptions("User not found")));
    return scoreRepository.save(scoreToUpdate);
  }

  @Override
  public Score getScoreById(Long scoreId) {
    return scoreRepository.findById(scoreId).orElseThrow(() -> new ResourceNotFoundExceptions("Score Not found!"));
  }

  @Override
  public List<Score> getScoreByUserId(Long userId) {
    try {
      return scoreRepository.findByUser_Id(userId);
    } catch (ResourceNotFoundExceptions e) {
      throw new ResourceNotFoundExceptions("Score not found");

    }
  }

  @Override
  public Score getScoreByRaceIdAndUserId(Long raceId, Long userId) {
    try {
      return scoreRepository.findByRace_IdAndUser_Id(raceId, userId);
    } catch (ResourceNotFoundExceptions e) {
      throw new ResourceNotFoundExceptions("Resource not found");
    }
  }

  @Override
  public List<Score> getScoreByRaceId(Long raceId) {
    try {
      return scoreRepository.findByRace_Id(raceId);
    } catch (ResourceNotFoundExceptions e) {
      throw new ResourceNotFoundExceptions("Resource not found");
    }
  }
}
