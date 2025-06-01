package com.example.moodmovies.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Film listesi görünümü için özet DTO.
 * Filmlerin listelendiği sayfalarda kullanılır.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmSummaryDTO {
    private String id;
    private String title;
    private String imageUrl; // Resim endpoint'ine işaret eden URL
}
