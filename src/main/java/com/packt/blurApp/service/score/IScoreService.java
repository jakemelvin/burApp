package com.packt.blurApp.service.score;

import java.util.List;

import com.packt.blurApp.dto.Score.AddScoreDto;
import com.packt.blurApp.model.Score;

public interface IScoreService {
  Score getScoreById(Long scoreId);

  Score addScore(AddScoreDto addScoreDto);

  Score updateScore(AddScoreDto updateScoreDto, Long scoreId);

  List<Score> getScoreByUserId(Long userId);

  Score getScoreByRaceIdAndUserId(Long raceId, Long userId);

  List<Score> getScoreByRaceId(Long raceId);
}
