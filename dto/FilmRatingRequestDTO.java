package com.example.moodmovies.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilmRatingRequestDTO {
    @NotNull(message = "Puan boş olamaz.")
    @Min(value = 1, message = "Puan en az 1 olmalıdır.")
    @Max(value = 10, message = "Puan en fazla 10 olmalıdır.") // Skalanıza göre ayarlayın
    private Integer rating;
}