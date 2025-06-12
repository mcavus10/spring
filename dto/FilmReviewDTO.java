package com.example.moodmovies.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmReviewDTO {
    private String id; // FilmPoint ID'si
    private UserSummaryDTO user; // Yorumu yapan kullanıcı
    private Integer rating; // Kullanıcının verdiği puan (nullable)
    private String text; // Kullanıcının yorumu (nullable) 
    private LocalDateTime created; // Yorum tarihi
    private Integer likes; // İleride beğeni sistemi için (şimdilik 0)
} 