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
public class RatedFilmDTO {
    private FilmSummaryDTO film; // Filmin özet bilgileri (id, title, imageUrl)
    private Integer userRating;    // Kullanıcının verdiği puan
    private String userComment;    // Kullanıcının yaptığı yorum
    private LocalDateTime ratedDate;   // Puanlama/son güncelleme tarihi
}