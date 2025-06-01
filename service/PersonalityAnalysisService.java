package com.example.moodmovies.service;

import com.example.moodmovies.dto.AnalysisResponse;
import com.example.moodmovies.exception.AiServiceException;
import com.example.moodmovies.exception.UserNotFoundException;

/**
 * Service interface for analyzing user personality profiles.
 */
public interface PersonalityAnalysisService {

    /**
     * Analyzes a user's personality profile by requesting it from the AI service.
     *
     * @param userId The ID of the user to analyze
     * @return Analysis response from the AI service
     * @throws UserNotFoundException if the user with the provided ID does not exist
     * @throws AiServiceException if there's an error communicating with the AI service
     */
    AnalysisResponse analyzeUserPersonality(String userId) throws UserNotFoundException, AiServiceException;
}
