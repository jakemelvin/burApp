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
        
        // New rule: any joined race participant can submit scores.
        if (!race.isParticipant(currentUser)) {
            throw new ForbiddenException("Only joined race participants can submit scores for this race");
        }
        
        // Check if score already exists for this user in this race
        if (scoreRepository.existsByRaceIdAndUserId(addScoreDto.getRaceId(), addScoreDto.getUserId())) {
            throw new ConflictException("Score already exists for this user in this race. Use update instead.");
        }
        
        // Calculate score based on rank: points = maxParticipants - rank + 1
        int maxParticipants = race.getParticipants().size();
        int rank = addScoreDto.getValue();
        
        // Validate rank is within valid bounds (1 to maxParticipants)
        if (rank < 1) {
            throw new BadRequestException("Le rang doit être au minimum 1");
        }
        if (rank > maxParticipants) {
            throw new BadRequestException(
                String.format("Le rang doit être au maximum %d (nombre de participants)", maxParticipants));
        }
        
        int calculatedPoints = maxParticipants - rank + 1;
        
        // Create score
        Score score = Score.builder()
                .race(race)
                .user(scoreUser)
                .submittedBy(currentUser)
                .value(calculatedPoints)
                .rank(rank)
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
        
        // New rule: any joined race participant can update scores.
        if (!score.getRace().isParticipant(currentUser)) {
            throw new ForbiddenException("Only joined race participants can update scores");
        }
        
        // Verify race is not cancelled
        if (score.getRace().getStatus() == RaceStatus.CANCELLED) {
            throw new BadRequestException("Cannot update scores for cancelled races");
        }
        
        // Calculate score based on rank: points = maxParticipants - rank + 1
        int maxParticipants = score.getRace().getParticipants().size();
        int rank = updateScoreDto.getValue();
        
        // Validate rank is within valid bounds (1 to maxParticipants)
        if (rank < 1) {
            throw new BadRequestException("Le rang doit être au minimum 1");
        }
        if (rank > maxParticipants) {
            throw new BadRequestException(
                String.format("Le rang doit être au maximum %d (nombre de participants)", maxParticipants));
        }
        
        int calculatedPoints = maxParticipants - rank + 1;
        
        score.setValue(calculatedPoints);
        score.setRank(rank);
        Score updatedScore = scoreRepository.save(score);
        
        log.info("Score {} updated successfully to {} points (rank: {})", scoreId, calculatedPoints, rank);
        return updatedScore;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Score> getScoresByUserId(Long userId) {
        log.debug("Fetching scores for user {}", userId);
        return scoreRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Score getScoreByRaceIdAndUserId(Long raceId, Long userId) {
        log.debug("Fetching score for race {} and user {}", raceId, userId);
        return scoreRepository.findByRaceIdAndUserId(raceId, userId)
                .orElseThrow(() -> new ResourceNotFoundExceptions(
                        "Score not found for race " + raceId + " and user " + userId));
    }

    @Override
    @Transactional(readOnly = true)
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
        
        // New rule: any joined race participant can delete scores.
        if (!score.getRace().isParticipant(currentUser)) {
            throw new ForbiddenException("Only joined race participants can delete scores");
        }
        
        scoreRepository.delete(score);
        log.info("Score {} deleted successfully", scoreId);
    }
}
