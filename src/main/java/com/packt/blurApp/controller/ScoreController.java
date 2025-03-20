package com.packt.blurApp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.packt.blurApp.dto.Score.AddScoreDto;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.mapper.scoreMapper.ScoreMapper;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.score.IScoreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/scores")
@RequiredArgsConstructor
public class ScoreController {
  private final IScoreService scoreService;

  @PostMapping("/add")
  public ResponseEntity<ApiResponse> addScore(@RequestBody AddScoreDto addScoreDto) {
    try {
      return ResponseEntity.ok(new ApiResponse("Score added successfully",
          ScoreMapper.toScoreResponseDto(scoreService.addScore(addScoreDto))));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @PutMapping("/update")
  public ResponseEntity<ApiResponse> updateScore(@RequestParam Long scoreId, @RequestBody AddScoreDto updateScoreDto) {
    try {
      return ResponseEntity
          .ok(new ApiResponse("Score Updated Successfully",
              ScoreMapper.toScoreResponseDto(scoreService.updateScore(updateScoreDto, scoreId))));
    } catch (ResourceNotFoundExceptions e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @GetMapping("/get-by-id")
  public ResponseEntity<ApiResponse> getScoreById(@RequestParam Long scoreId) {
    try {
      return ResponseEntity.ok(new ApiResponse("Score fetched successfully",
          ScoreMapper.toScoreResponseDto(scoreService.getScoreById(scoreId))));
    } catch (ResourceNotFoundExceptions e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @GetMapping("/get-by-user-id")
  public ResponseEntity<ApiResponse> getScoreByUserId(@RequestParam Long userId) {
    try {
      return ResponseEntity.ok(new ApiResponse("Score fetched successfully",
          ScoreMapper.toScoreResponseDtoList(scoreService.getScoreByUserId(userId))));
    } catch (ResourceNotFoundExceptions e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @GetMapping("/get-by-race-and-user-id")
  public ResponseEntity<ApiResponse> getScoreByRaceAndUserId(@RequestParam Long raceId, @RequestParam Long userId) {
    try {
      return ResponseEntity
          .ok(new ApiResponse("Score fetched successfully",
              ScoreMapper.toScoreResponseDto(scoreService.getScoreByRaceIdAndUserId(raceId, userId))));
    } catch (ResourceNotFoundExceptions e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }

  @GetMapping("/get-by-race-id")
  public ResponseEntity<ApiResponse> getScoreByRaceId(@RequestParam Long raceId) {
    try {
      return ResponseEntity.ok(new ApiResponse("Score fetched successfully",
          ScoreMapper.toScoreResponseDtoList(scoreService.getScoreByRaceId(raceId))));
    } catch (ResourceNotFoundExceptions e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }
}
