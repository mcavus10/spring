package com.example.moodmovies.controller;

import com.example.moodmovies.dto.FilmDetailDTO;
import com.example.moodmovies.dto.FilmSummaryDTO;
import com.example.moodmovies.service.FilmService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Film verileri için REST API endpoint'lerini sağlayan Controller.
 * /api/v1/films path prefix'i ile erişilir.
 */
@RestController
@RequestMapping("/api/v1/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    /**
     * Filmlerin özet bilgilerini sayfalanmış olarak listeler.
     * Örnek istek: /api/v1/films?page=0&size=10&sort=releaseDate,desc
     * @param pageable Spring tarafından otomatik doldurulan sayfalama ve sıralama bilgisi
     * @return Sayfalanış özet film bilgilerinin listesi
     */
    @GetMapping
    public ResponseEntity<Page<FilmSummaryDTO>> getAllFilmSummaries(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) { // Varsayılan 20 film, ID'ye göre sıralı
        Page<FilmSummaryDTO> summariesPage = filmService.getFilmSummaries(pageable);
        return ResponseEntity.ok(summariesPage);
    }

    /**
     * ID'ye göre film detaylarını getirir
     * @param id Film ID'si
     * @return Film detayları
     */
    @GetMapping("/{id}")
    public ResponseEntity<FilmDetailDTO> getFilmDetails(@PathVariable String id) {
        FilmDetailDTO detail = filmService.getFilmDetailById(id);
        return ResponseEntity.ok(detail);
    }

    /**
     * Film ID'sine göre resim verisini getirir
     * @param id Film ID'si
     * @return Resim binary verisi
     */
    @GetMapping(value = "/image/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getFilmImage(@PathVariable String id) {
        byte[] imageBytes = filmService.getFilmImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // İçeriğin tipini belirtin
                .body(imageBytes);
    }
    
    /**
     * Verilen sorguyla eşleşen film önerilerini (en fazla 5) döndürür.
     * Arama çubuğunda yazarken kullanılır.
     * Örnek istek: /api/v1/films/suggestions?query=matrix
     * @param query Aranacak film adı parçası (en az 2 karakter olması önerilir - frontend kontrol etmeli)
     * @return Bulunan film önerilerinin listesi (ID, Ad, Resim URL)
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<FilmSummaryDTO>> getSuggestions(
            @RequestParam("query") String query) {
        // Sorgu çok kısaysa boş liste döndür (opsiyonel backend kontrolü)
        if (query == null || query.trim().length() < 2) { // Örneğin en az 2 karakter şartı
             return ResponseEntity.ok(List.of()); // Boş liste döndür
        }
        List<FilmSummaryDTO> suggestions = filmService.getFilmSuggestions(query.trim());
        return ResponseEntity.ok(suggestions);
    }
}
