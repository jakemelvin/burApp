package com.packt.blurApp.service.score;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packt.blurApp.dto.Score.AddScoreDto;
import com.packt.blurApp.exceptions.BadRequestException;
import com.packt.blurApp.exceptions.ConflictException;
import com.packt.blurApp.exceptions.ForbiddenException;
import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.Race;
import com.packt.blurApp.model.Score;
import com.packt.blurApp.model.User;
import com.packt.blurApp.model.enums.RaceStatus;
import com.packt.blurApp.repository.RaceRepository;
import com.packt.blurApp.repository.ScoreRepository;
import com.packt.blurApp.repository.UserRepository;
import com.packt.blurApp.service.user.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreService implements IScoreService {
    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final RaceRepository raceRepository;
    private final IUserService userService;

    @Override
    public Score getScoreById(Long scoreId) {
        log.debug("Fetching score by ID: {}", scoreId);
        return scoreRepository.findById(scoreId)
                .orElseThrow(() -> new ResourceNotFoundExceptions("Score not found with ID: " + scoreId));
    }

    @Override
    @Transactional
    public Score submitScore(AddScoreDto addScoreDto) {
        log.info("Submitting score for race {} and user {}", addScoreDto.getRaceId(), addScoreDto.getUserId());
        
        User currentUser = userService.getCurrentUser();
        
        Race race = raceRepository.findById(addScoreDto.getRaceId())
                .orElseThrow(() -> new ResourceNotFoundExceptions("Race not found with ID: " + addScoreDto.getRaceId()));
        
        User scoreUser = userRepository.findById(addScoreDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundExceptions("User not found with ID: " + addScoreDto.getUserId()));
        
        // Verify race is in progress or completed
        if (race.getStatus() != RaceStatus.IN_PROGRESS && race.getStatus() != RaceStatus.COMPLETED) {
            throw new BadRequestException("Can only submit scores for races that are in progress or completed");
        }
        
        // Verify the score user is a participant
        if (!race.isParticipant(scoreUser)) {
            throw new BadRequestException("User is not a participant in this race");
        }
        
        // Only the assigned score collector can submit scores
        if (!race.getScoreCollector().equals(currentUser)) {
            throw new ForbiddenException("Only the assigned score collector can submit scores for this race");
        }
        
        // Check if score already exists for this user in this race
        if (scoreRepository.existsByRaceIdAndUserId(addScoreDto.getRaceId(), addScoreDto.getUserId())) {
            throw new ConflictException("Score already exists for this user in this race. Use update instead.");
        }
        
        // Create score
        Score score = Score.builder()
                .race(race)
                .user(scoreUser)
                .submittedBy(currentUser)
                .value(addScoreDto.getValue())
                .build();
        
        Score savedScore = scoreRepository.save(score);
        race.addScore(savedScore);
        
        log.info("Score submitted successfully: {} points for user {} in race {}", 
                addScoreDto.getValue(), scoreUser.getUsername(), race.getId());
        
        return savedScore;
    }

    @Override
    @Transactional
    public Score updateScore(Long scoreId, AddScoreDto updateScoreDto) {
        log.info("Updating score {}", scoreId);
        
        User currentUser = userService.getCurrentUser();
        Score score = getScoreById(scoreId);
        
        // Only the score collector or party managers can update scores
        if (!score.getRace().getScoreCollector().equals(currentUser) && 
            !score.getRace().getParty().isManager(currentUser)) {
            throw new ForbiddenException("Only the score collector or party managers can update scores");
        }
        
        // Verify race is not cancelled
        if (score.getRace().getStatus() == RaceStatus.CANCELLED) {
            throw new BadRequestException("Cannot update scores for cancelled races");
        }
        
        score.setValue(updateScoreDto.getValue());
        Score updatedScore = scoreRepository.save(score);
        
        log.info("Score {} updated successfully to {} points", scoreId, updateScoreDto.getValue());
        return updatedScore;
    }

    @Override
    public List<Score> getScoresByUserId(Long userId) {
        log.debug("Fetching scores for user {}", userId);
        return scoreRepository.findByUserId(userId);
    }

    @Override
    public Score getScoreByRaceIdAndUserId(Long raceId, Long userId) {
        log.debug("Fetching score for race {} and user {}", raceId, userId);
        return scoreRepository.findByRaceIdAndUserId(raceId, userId)
                .orElseThrow(() -> new ResourceNotFoundExceptions(
                        "Score not found for race " + raceId + " and user " + userId));
    }

    @Override
    public List<Score> getScoresByRaceId(Long raceId) {
        log.debug("Fetching all scores for race {}", raceId);
        return scoreRepository.findByRaceId(raceId);
    }

    @Override
    @Transactional
    public void deleteScore(Long scoreId) {
        log.info("Deleting score {}", scoreId);
        
        User currentUser = userService.getCurrentUser();
        Score score = getScoreById(scoreId);
        
        // Only party managers can delete scores
        if (!score.getRace().getParty().isManager(currentUser)) {
            throw new ForbiddenException("Only party managers can delete scores");
        }
        
        scoreRepository.delete(score);
        log.info("Score {} deleted successfully", scoreId);
    }
}
