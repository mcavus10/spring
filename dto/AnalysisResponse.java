package com.example.moodmovies.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing personality analysis results from the AI service.
 * Aligned with Python API v1.2 response format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {
    private String message;
    
    @JsonProperty("profile_id") // Python API'den gelen snake_case alan adı
    private String profileId; // Java tarafında camelCase değişken adı
    
    private ScoreResult scores; // Python'dan gelen scores nesnesi
}
