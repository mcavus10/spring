package com.example.moodmovies.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Film detay sayfası için ayrıntılı DTO.
 * Bir filmin tüm detaylarını içerir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmDetailDTO {
    private String id;
    private String title;
    private String imageUrl;
    private BigDecimal rating;
    private String releaseYear; // Sadece yıl
    private String country; // View'dan direkt alınabilir
    private String formattedDuration; // "X S Y DK" formatında
    private String plot;
    private List<String> genres;
    private BigDecimal averageRating;
}
