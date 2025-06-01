package com.example.moodmovies.service;

import com.example.moodmovies.dto.FilmSummaryDTO;
import com.example.moodmovies.dto.RatedFilmDTO;
import com.example.moodmovies.dto.UserFilmInteractionDTO;
// Gerekirse UserNotFoundException, FilmNotFoundException importları

import java.util.List;

public interface UserInteractionService {

    /**
     * Bir kullanıcı bir filme puan verir veya mevcut puanını günceller.
     * @param userId Kullanıcı ID'si
     * @param filmId Film ID'si
     * @param rating Verilen puan
     * @return Filmin güncel kullanıcı etkileşim durumu
     */
    UserFilmInteractionDTO rateFilm(String userId, String filmId, int rating);

    /**
     * Bir kullanıcının bir film için favori durumunu değiştirir (ekler/kaldırır).
     * @param userId Kullanıcı ID'si
     * @param filmId Film ID'si
     * @return Filmin güncel kullanıcı etkileşim durumu
     */
    UserFilmInteractionDTO toggleFavorite(String userId, String filmId);

    /**
     * Bir kullanıcının belirli bir filmle olan etkileşim durumunu (puanı, favori mi)
     * ve filmin genel ortalama puanını getirir.
     * @param userId Kullanıcı ID'si
     * @param filmId Film ID'si
     * @return Kullanıcı-Film etkileşim DTO'su
     */
    UserFilmInteractionDTO getUserFilmInteractionStatus(String userId, String filmId);

    /**
     * Bir kullanıcının favorilediği tüm filmleri listeler.
     * @param userId Kullanıcı ID'si
     * @return Favori filmlerin özet DTO listesi
     */
    List<FilmSummaryDTO> getUserFavoriteFilms(String userId);

    /**
     * Bir kullanıcının son puanladığı filmleri, puanlarıyla birlikte limitli sayıda getirir.
     * @param userId Kullanıcı ID'si
     * @param limit Getirilecek maksimum film sayısı
     * @return Puanlanmış filmlerin DTO listesi
     */
    List<RatedFilmDTO> getLatestRatedFilms(String userId, int limit);
}