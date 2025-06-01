package com.example.moodmovies.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for representing personality scores.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreResult {
    // Ana domain skorları
    private BigDecimal o; // Openness
    private BigDecimal c; // Conscientiousness
    private BigDecimal e; // Extraversion
    private BigDecimal a; // Agreeableness
    private BigDecimal n; // Neuroticism
    
    // Alt özelliklerin (facets) skorları
    private Map<String, BigDecimal> facets;
}
