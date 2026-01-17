package com.packt.blurApp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.packt.blurApp.dto.Score.AddScoreDto;
import com.packt.blurApp.mapper.scoreMapper.ScoreMapper;
import com.packt.blurApp.model.Score;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.score.IScoreService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/scores")
@RequiredArgsConstructor
public class ScoreController {
    private final IScoreService scoreService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_SCORE')")
    public ResponseEntity<ApiResponse<?>> getScoreById(@PathVariable Long id) {
        log.info("GET ${api.prefix}/scores/{} - Get score by ID", id);
        Score score = scoreService.getScoreById(id);
        return ResponseEntity.ok(ApiResponse.success("Score fetched successfully",
                ScoreMapper.toScoreResponseDto(score)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('VIEW_SCORE')")
    public ResponseEntity<ApiResponse<?>> getScoresByUserId(@PathVariable Long userId) {
        log.info("GET ${api.prefix}/scores/user/{} - Get scores by user ID", userId);
        return ResponseEntity.ok(ApiResponse.success("Scores fetched successfully",
                ScoreMapper.toScoreResponseDtoList(scoreService.getScoresByUserId(userId))));
    }

    @GetMapping("/race/{raceId}")
    @PreAuthorize("hasAuthority('VIEW_SCORE')")
    public ResponseEntity<ApiResponse<?>> getScoresByRaceId(@PathVariable Long raceId) {
        log.info("GET ${api.prefix}/scores/race/{} - Get scores by race ID", raceId);
        return ResponseEntity.ok(ApiResponse.success("Scores fetched successfully",
                ScoreMapper.toScoreResponseDtoList(scoreService.getScoresByRaceId(raceId))));
    }

    @GetMapping("/race/{raceId}/user/{userId}")
    @PreAuthorize("hasAuthority('VIEW_SCORE')")
    public ResponseEntity<ApiResponse<?>> getScoreByRaceIdAndUserId(
            @PathVariable Long raceId, @PathVariable Long userId) {
        log.info("GET ${api.prefix}/scores/race/{}/user/{} - Get score by race and user ID", raceId, userId);
        Score score = scoreService.getScoreByRaceIdAndUserId(raceId, userId);
        return ResponseEntity.ok(ApiResponse.success("Score fetched successfully",
                ScoreMapper.toScoreResponseDto(score)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUBMIT_SCORE')")
    public ResponseEntity<ApiResponse<?>> submitScore(@Valid @RequestBody AddScoreDto addScoreDto) {
        log.info("POST ${api.prefix}/scores - Submit score for race {} and user {}", 
                addScoreDto.getRaceId(), addScoreDto.getUserId());
        Score score = scoreService.submitScore(addScoreDto);
        return ResponseEntity.ok(ApiResponse.success("Score submitted successfully",
                ScoreMapper.toScoreResponseDto(score)));
    }

    @PutMapping("/{scoreId}")
    @PreAuthorize("hasAuthority('EDIT_SCORE')")
    public ResponseEntity<ApiResponse<?>> updateScore(
            @PathVariable Long scoreId, @Valid @RequestBody AddScoreDto updateScoreDto) {
        log.info("PUT ${api.prefix}/scores/{} - Update score", scoreId);
        Score score = scoreService.updateScore(scoreId, updateScoreDto);
        return ResponseEntity.ok(ApiResponse.success("Score updated successfully",
                ScoreMapper.toScoreResponseDto(score)));
    }

    @DeleteMapping("/{scoreId}")
    @PreAuthorize("hasAuthority('EDIT_SCORE')")
    public ResponseEntity<ApiResponse<?>> deleteScore(@PathVariable Long scoreId) {
        log.info("DELETE ${api.prefix}/scores/{} - Delete score", scoreId);
        scoreService.deleteScore(scoreId);
        return ResponseEntity.ok(ApiResponse.success("Score deleted successfully"));
    }
}
