package com.example.moodmovies.controller;

import com.example.moodmovies.dto.FilmRatingRequestDTO;
import com.example.moodmovies.dto.FilmSummaryDTO;
import com.example.moodmovies.dto.RatedFilmDTO;
import com.example.moodmovies.dto.UserFilmInteractionDTO;
import com.example.moodmovies.security.UserPrincipal;
import com.example.moodmovies.service.UserInteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
// import org.springframework.web.bind.annotation.RequestParam; // Zaten yukarıda var

import java.util.List;

@RestController
@RequestMapping("/api/v1/interactions")
@RequiredArgsConstructor
public class UserInteractionController {

    private final UserInteractionService userInteractionService;

    @PostMapping("/films/{filmId}/rate")
    public ResponseEntity<UserFilmInteractionDTO> rateFilm(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String filmId,
            @Valid @RequestBody FilmRatingRequestDTO ratingRequest) {
        UserFilmInteractionDTO updatedInteraction = userInteractionService.rateFilm(currentUser.getId(), filmId, ratingRequest.getRating());
        return ResponseEntity.ok(updatedInteraction);
    }

    @PostMapping("/films/{filmId}/favorite")
    public ResponseEntity<UserFilmInteractionDTO> toggleFavorite(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String filmId) {
        UserFilmInteractionDTO updatedInteraction = userInteractionService.toggleFavorite(currentUser.getId(), filmId);
        return ResponseEntity.ok(updatedInteraction);
    }

    @GetMapping("/films/{filmId}/status")
    public ResponseEntity<UserFilmInteractionDTO> getUserFilmInteractionStatus(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String filmId) {
        UserFilmInteractionDTO status = userInteractionService.getUserFilmInteractionStatus(currentUser.getId(), filmId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<FilmSummaryDTO>> getUserFavoriteFilms(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<FilmSummaryDTO> favorites = userInteractionService.getUserFavoriteFilms(currentUser.getId());
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/ratings/latest")
    public ResponseEntity<List<RatedFilmDTO>> getLatestRatedFilms(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        if (limit <= 0) {
            limit = 1; // Minimum 1 olmalı
        } else if (limit > 20) { // Performans için makul bir üst sınır
            limit = 20;
        }
        List<RatedFilmDTO> latestRatedFilms = userInteractionService.getLatestRatedFilms(currentUser.getId(), limit);
        return ResponseEntity.ok(latestRatedFilms);
    }
}