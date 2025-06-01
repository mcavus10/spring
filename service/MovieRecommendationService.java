package com.example.moodmovies.service;

import com.example.moodmovies.dto.FilmSummaryDTO;

import java.util.List;

/**
 * Service interface for movie recommendations.
 */
public interface MovieRecommendationService {
    
    /**
     * Gets personalized movie recommendations for a user.
     *
     * @param userId The ID of the user to get recommendations for
     * @return List of film summary DTOs as recommendations
     */
    List<FilmSummaryDTO> getUserRecommendations(String userId);
}
