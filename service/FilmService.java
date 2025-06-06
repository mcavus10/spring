package com.example.moodmovies.service;

import com.example.moodmovies.dto.FilmDetailDTO;
import com.example.moodmovies.dto.FilmSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Film veri erişim servisi.
 * Veritabanındaki film verilerine erişim sağlar ve frontend için uygun DTO'lara dönüştürür.
 */
public interface FilmService {
    /**
     * Tüm filmlerin özet bilgilerini sayfalanmış olarak getirir
     * @param pageable Sayfalama ve sıralama bilgisini içeren nesne
     * @return Film özetlerini içeren sayfa
     */
    Page<FilmSummaryDTO> getFilmSummaries(Pageable pageable);
    
    /**
     * ID'ye göre bir filmin detay bilgilerini getirir
     * @param filmId Film ID'si
     * @return Film detayları, film bulunamazsa FilmNotFoundException fırlatır
     */
    FilmDetailDTO getFilmDetailById(String filmId);
    
    /**
     * Belirtilen film ID'sine ait resim verisini getirir
     * @param filmId Film ID'si
     * @return Resim byte dizisi, resim bulunamazsa FilmNotFoundException fırlatır
     */
    byte[] getFilmImage(String filmId);
    
    /**
     * Verilen sorguyla eşleşen film önerilerini (en fazla 5) getirir.
     * @param query Aranacak film adı parçası
     * @return Film önerileri listesi (FilmSummaryDTO nesneleri)
     */
    List<FilmSummaryDTO> getFilmSuggestions(String query);

    BigDecimal getAverageRatingForFilm(String filmId);

    List<FilmSummaryDTO> getTopFavoritedFilms(int limit);
}
