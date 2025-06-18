package com.example.moodmovies.service.impl;

import com.example.moodmovies.dto.FilmSummaryDTO;
import com.example.moodmovies.model.FilmInfo;
import com.example.moodmovies.repository.FilmInfoRepository;
import com.example.moodmovies.repository.SuggestionRepository;
import com.example.moodmovies.service.MovieRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * Implementation of MovieRecommendationService for handling movie recommendations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MovieRecommendationServiceImpl implements MovieRecommendationService {

    private final SuggestionRepository suggestionRepository;
    private final FilmInfoRepository filmInfoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FilmSummaryDTO> getUserRecommendations(String userId) {
        log.info("Fetching film recommendations for user: {}", userId);

        // 1. Veritabanından önerilen film ID'lerini çek
        List<String> recommendedFilmIds = suggestionRepository.findLatestFilmIdsByUserId(userId);

        if (recommendedFilmIds == null || recommendedFilmIds.isEmpty()) {
            log.warn("No film suggestions found in database for user: {}", userId);
            return List.of(); // Boş liste döndür
        }

        // 2. Film ID'leri ile film detaylarını (FilmInfo) çek
        List<FilmInfo> recommendedFilmsInfo = filmInfoRepository.findAllById(recommendedFilmIds);

        if (recommendedFilmsInfo.isEmpty()) {
            log.warn("Found recommendation IDs for user {} but no matching films in FilmInfo.", userId);
            return List.of();
        }

        // 3. FilmInfo listesini FilmSummaryDTO listesine dönüştür
        List<FilmSummaryDTO> dtos = recommendedFilmsInfo.stream()
                .map(this::convertToSummaryDTO)
                .toList();

        log.info("Returning {} film recommendations for user: {}", dtos.size(), userId);
        return dtos;
    }

    /**
     * Helper method to convert a FilmInfo entity to a FilmSummaryDTO.
     *
     * @param filmInfo The FilmInfo entity to convert
     * @return FilmSummaryDTO with basic film information
     */
    private FilmSummaryDTO convertToSummaryDTO(FilmInfo filmInfo) {
        String baseUrl;
        try {
            baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        } catch (Exception e) {
            baseUrl = "http://localhost:8080";
        }
        return FilmSummaryDTO.builder()
                .id(filmInfo.getId())
                .title(filmInfo.getName())
                .imageUrl(baseUrl + "/api/v1/films/image/" + filmInfo.getId()) // Tam, dinamik URL
                .build();
    }
}