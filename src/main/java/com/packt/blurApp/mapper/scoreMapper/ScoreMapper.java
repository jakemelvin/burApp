package com.packt.blurApp.mapper.scoreMapper;

import java.util.List;

import com.packt.blurApp.dto.Score.ScoreResponseDto;
import com.packt.blurApp.mapper.userMapper.UserResponseMapper;
import com.packt.blurApp.model.Score;

public class ScoreMapper {
  public static ScoreResponseDto toScoreResponseDto(Score score) {
    ScoreResponseDto dto = new ScoreResponseDto();
    dto.setId(score.getId());
    dto.setValue(score.getValue());
    dto.setUser(score.getUser() != null ? UserResponseMapper.toUserResponseDto(score.getUser()) : null);
    return dto;
  }

  public static List<ScoreResponseDto> toScoreResponseDtoList(List<Score> scores) {
    return scores.stream()
        .sorted(java.util.Comparator.comparing(Score::getId))
        .map(ScoreMapper::toScoreResponseDto)
        .toList();
  }
}
