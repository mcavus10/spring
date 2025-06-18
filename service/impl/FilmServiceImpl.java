package com.example.moodmovies.service.impl;

import com.example.moodmovies.dto.FilmDetailDTO;
import com.example.moodmovies.dto.FilmSummaryDTO;
import com.example.moodmovies.exception.FilmNotFoundException;
import com.example.moodmovies.model.FilmInfo;
import com.example.moodmovies.repository.FilmInfoRepository;
import com.example.moodmovies.repository.FilmPointRepository;
import com.example.moodmovies.service.FilmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * FilmService arayüzünün implementasyonu.
 * Film verilerini veritabanından çeker ve DTO'lara dönüştürür.
 */
@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {

    private final FilmInfoRepository filmInfoRepository;
    private final FilmPointRepository filmPointRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    @Transactional(readOnly = true) // Veritabanından sadece okuma
    public Page<FilmSummaryDTO> getFilmSummaries(Pageable pageable) {
        // Repository'den Page<FilmInfo> olarak veriyi çek
        Page<FilmInfo> filmInfoPage = filmInfoRepository.findAll(pageable);

        // Page<FilmInfo>'yu Page<FilmSummaryDTO>'ya dönüştür
        // map fonksiyonu Page içindeki içerikleri dönüştürmek için kullanılır
        return filmInfoPage.map(this::convertToSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public FilmDetailDTO getFilmDetailById(String filmId) {
        FilmInfo filmInfo = filmInfoRepository.findById(filmId)
                .orElseThrow(() -> new FilmNotFoundException("Film not found with id: " + filmId));
        return convertToDetailDTO(filmInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getFilmImage(String filmId) {
        FilmInfo filmInfo = filmInfoRepository.findById(filmId)
                .orElseThrow(() -> new FilmNotFoundException("Film image not found for id: " + filmId));
        return filmInfo.getImageByte();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<FilmSummaryDTO> getFilmSuggestions(String query) {
        // Repository'deki yeni metodu çağır (en fazla 5 sonuç dönecek)
        List<FilmInfo> filmInfoList = filmInfoRepository.findTop5ByNameContainingIgnoreCaseOrderById(query);

        // Sonuçları DTO listesine map et
        return filmInfoList.stream()
                         .map(this::convertToSummaryDTO) // Mevcut DTO dönüştürme metodunu kullan
                         .collect(Collectors.toList());
    }

    // --- Helper Metotlar ---

    private FilmSummaryDTO convertToSummaryDTO(FilmInfo filmInfo) {
        return FilmSummaryDTO.builder()
                .id(filmInfo.getId())
                .title(filmInfo.getName())
                .imageUrl(generateImageUrl(filmInfo.getId()))
                .build();
    }

    private FilmDetailDTO convertToDetailDTO(FilmInfo filmInfo) {
        return FilmDetailDTO.builder()
                .id(filmInfo.getId())
                .title(filmInfo.getName())
                .imageUrl(generateImageUrl(filmInfo.getId()))
                .rating(filmInfo.getRating())
                .releaseYear(filmInfo.getReleaseDate() != null ? String.valueOf(filmInfo.getReleaseDate().getYear()) : null)
                .country(filmInfo.getCountry())
                .formattedDuration(formatDuration(filmInfo.getRuntime()))
                .plot(filmInfo.getPlot())
                .genres(extractGenres(filmInfo))
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageRatingForFilm(String filmId) {
        // Film var mı diye kontrol (isteğe bağlı ama önerilir)
        if (!filmInfoRepository.existsById(filmId)) {
            throw new FilmNotFoundException("Film bulunamadı: " + filmId);
        }
        Double avgRatingRaw = filmPointRepository.findAverageRatingByFilmId(filmId).orElse(0.0);
        return BigDecimal.valueOf(avgRatingRaw).setScale(1, RoundingMode.HALF_UP);
    }


    private String generateImageUrl(String filmId) {
        return baseUrl + "/api/v1/films/image/" + filmId;
    }

    private String formatDuration(Integer runtimeMinutes) {
        if (runtimeMinutes == null || runtimeMinutes <= 0) {
            return null; // Veya "N/A" gibi bir değer
        }
        int hours = runtimeMinutes / 60;
        int minutes = runtimeMinutes % 60;
        // Frontend'in beklediği format "X S Y DK"
        return String.format("%d S %dDK", hours, minutes);
    }

    private List<String> extractGenres(FilmInfo filmInfo) {
        // TUR_1, TUR_2, TUR_3, TUR_4 alanlarından null olmayanları listeye ekle
        return Stream.of(filmInfo.getTur1(), filmInfo.getTur2(), filmInfo.getTur3(), filmInfo.getTur4())
                .filter(Objects::nonNull) // Null olmayanları filtrele
                .filter(s -> !s.trim().isEmpty()) // Boş stringleri filtrele (isteğe bağlı)
                .collect(Collectors.toList());
    }

    @Override
@Transactional(readOnly = true)
public List<FilmSummaryDTO> getTopFavoritedFilms(int limit) {
    Pageable pageable = PageRequest.of(0, limit);
    List<Object[]> favoritedResults = filmPointRepository.findTopFavoritedFilmIds(pageable);

    if (favoritedResults.isEmpty()) {
        return Collections.emptyList();
    }

    List<String> filmIds = favoritedResults.stream()
            .map(result -> (String) result[0])
            .collect(Collectors.toList());

    List<FilmInfo> filmInfos = filmInfoRepository.findAllById(filmIds);

    // Orijinal favori sırasını korumak için bir Map kullanalım
    Map<String, FilmInfo> filmInfoMap = filmInfos.stream()
            .collect(Collectors.toMap(FilmInfo::getId, film -> film));

    // Orijinal sıralamaya göre DTO listesi oluştur
    return filmIds.stream()
            .map(filmInfoMap::get)
            .filter(Objects::nonNull)
            .map(this::convertToSummaryDTO) // Zaten var olan helper metot
            .collect(Collectors.toList());
}
}
