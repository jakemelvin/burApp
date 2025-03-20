package com.packt.blurApp.mapper.scoreMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.packt.blurApp.dto.Score.ScoreResponseDto;
import com.packt.blurApp.mapper.userMapper.UserResponseMapper;
import com.packt.blurApp.model.Score;

public class ScoreMapper {
  public static ScoreResponseDto toScoreResponseDto(Score score) {
    ScoreResponseDto dto = new ScoreResponseDto();
    dto.setId(score.getId());
    dto.setValue(score.getValue());
    dto.setUser(UserResponseMapper.toUserResponseDto(score.getUser()));
    return dto;
  }

  public static Set<ScoreResponseDto> toScoreResponseDtoList(List<Score> scores) {
    Set<ScoreResponseDto> dtoList = new HashSet<>();
    scores.forEach(score -> {
      dtoList.add(toScoreResponseDto(score));
    });
    return dtoList;
  }
}
