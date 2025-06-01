package com.example.moodmovies.controller;

import com.example.moodmovies.dto.FilmSummaryDTO;
import com.example.moodmovies.security.UserPrincipal;
import com.example.moodmovies.service.MovieRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for handling movie recommendations.
 */
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final MovieRecommendationService recommendationService;

    /**
     * Endpoint for retrieving personalized movie recommendations for the authenticated user.
     *
     * @param currentUser The authenticated user (injected by Spring Security)
     * @return Response with list of recommended films or appropriate error status
     */
    @GetMapping
    public ResponseEntity<List<FilmSummaryDTO>> getUserRecommendations(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<FilmSummaryDTO> recommendations = recommendationService.getUserRecommendations(currentUser.getId());
            if (recommendations.isEmpty()) {
                // Öneri yoksa 204 No Content döndürmek daha uygun olabilir
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("Error fetching recommendations for user {}: {}", currentUser.getId(), e.getMessage(), e);
            // Genel bir hata mesajı veya ErrorDetail döndürülebilir
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
