package com.packt.blurApp.service.score;

import java.util.List;

import org.springframework.stereotype.Service;

import com.packt.blurApp.dto.Score.AddScoreDto;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.Score;
import com.packt.blurApp.repository.ScoreRepository;
import com.packt.blurApp.service.race.IRaceService;
import com.packt.blurApp.service.user.IUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScoreService implements IScoreService {

  private final ScoreRepository scoreRepository;
  private final IUserService userService;
  private final IRaceService raceService;

  @Override
  public Score addScore(AddScoreDto addScoreDto) {
    try {
      Score score = new Score();
      score.setUser(userService.getUserById(addScoreDto.getUserId()));
      score.setRace(raceService.getRaceById(addScoreDto.getRaceId()));
      score.setValue(0);
      return scoreRepository.save(score);
    } catch (ResourceNotFoundExceptions e) {
      throw new ResourceNotFoundExceptions(e.getMessage());
    }
  }

  @Override
  public Score updateScore(AddScoreDto updateScoreDto, Long scoreId) {
    Score scoreToUpdate = getScoreById(scoreId);
    scoreToUpdate.setValue(updateScoreDto.getValue());
    scoreToUpdate.setUser(userService.getUserById(updateScoreDto.getUserId()));
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
