package com.example.moodmovies.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Bir listeye film ekleme isteği için DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilmToListRequestDTO {

    @NotBlank(message = "Film ID'si (filmId) boş bırakılamaz.")
    private String filmId;
}