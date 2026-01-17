package com.packt.blurApp.service.score;

import java.util.List;

import com.packt.blurApp.dto.Score.AddScoreDto;
import com.packt.blurApp.model.Score;

public interface IScoreService {
    Score getScoreById(Long scoreId);
    
    Score submitScore(AddScoreDto addScoreDto);
    
    Score updateScore(Long scoreId, AddScoreDto updateScoreDto);
    
    List<Score> getScoresByUserId(Long userId);
    
    Score getScoreByRaceIdAndUserId(Long raceId, Long userId);
    
    List<Score> getScoresByRaceId(Long raceId);
    
    void deleteScore(Long scoreId);
}
