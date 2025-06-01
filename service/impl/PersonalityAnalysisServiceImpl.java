package com.example.moodmovies.service.impl;

import com.example.moodmovies.client.AiServiceClient;
import com.example.moodmovies.dto.AnalysisResponse;
import com.example.moodmovies.exception.AiServiceException;
import com.example.moodmovies.exception.UserNotFoundException;
import com.example.moodmovies.repository.UserRepository;
import com.example.moodmovies.service.PersonalityAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of PersonalityAnalysisService for analyzing user personality profiles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalityAnalysisServiceImpl implements PersonalityAnalysisService {

    private final AiServiceClient aiServiceClient;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public AnalysisResponse analyzeUserPersonality(String userId) throws UserNotFoundException, AiServiceException {
        // Verify the user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found while attempting personality analysis: {}", userId);
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        
        log.info("Requesting personality analysis for user: {}", userId);
        
        try {
            // Request analysis from AI service
            AnalysisResponse response = aiServiceClient.requestPersonalityAnalysis(userId);
            
            if (response == null) {
                throw new AiServiceException("AI service returned null response");
            }
            
            log.info("Successfully received personality analysis for user: {}", userId);
            return response;
            
        } catch (AiServiceException e) {
            // Let the AiServiceException propagate up
            log.error("AI service error for user {}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // Wrap any other exceptions in AiServiceException
            log.error("Unexpected error during personality analysis for user {}: {}", userId, e.getMessage());
            throw new AiServiceException("Unexpected error during personality analysis: " + e.getMessage(), e);
        }
    }
}
