package com.packt.blurApp.service.score;

import com.packt.blurApp.dto.Score.AddScoreDto;
import com.packt.blurApp.model.Score;

public interface IScoreRepository {
  Score getScoreById(Long scoreId);

  Score addScore(AddScoreDto addScoreDto);

  Score updateScore(AddScoreDto updateScoreDto, Long scoreId);
}
