package com.example.moodmovies.service.impl;

import com.example.moodmovies.dto.FilmReviewDTO;
import com.example.moodmovies.dto.FilmSummaryDTO;
import com.example.moodmovies.dto.RatedFilmDTO;
import com.example.moodmovies.dto.UserFilmInteractionDTO;
import com.example.moodmovies.dto.UserSummaryDTO;
import com.example.moodmovies.exception.FilmNotFoundException;
import com.example.moodmovies.exception.UserNotFoundException;
import com.example.moodmovies.model.FilmInfo;
import com.example.moodmovies.model.FilmPoint;
import com.example.moodmovies.model.User;
import com.example.moodmovies.repository.FilmInfoRepository;
import com.example.moodmovies.repository.FilmPointRepository;
import com.example.moodmovies.repository.UserRepository;
import com.example.moodmovies.service.FilmService;
import com.example.moodmovies.service.UserInteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserInteractionServiceImpl implements UserInteractionService {

    private final UserRepository userRepository;
    private final FilmPointRepository filmPointRepository;
    private final FilmInfoRepository filmInfoRepository;
    private final FilmService filmService; // Ortalama puanı almak için

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 10;

    @Override
    public UserFilmInteractionDTO rateFilm(String userId, String filmId, int rating, String comment) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException("Puan " + MIN_RATING + " ile " + MAX_RATING + " arasında olmalıdır.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));

        if (!filmInfoRepository.existsById(filmId)) {
            throw new FilmNotFoundException("Film bulunamadı: " + filmId);
        }

        FilmPoint filmPoint = filmPointRepository.findByUserAndFilmId(user, filmId)
                .orElseGet(() -> FilmPoint.builder().user(user).filmId(filmId).build());

        filmPoint.setFilmPoint(rating);
        filmPoint.setComment(comment);
        filmPointRepository.save(filmPoint);
        
        if (comment != null && !comment.trim().isEmpty()) {
            log.info("Kullanıcı {} filme {} puan verdi: {} ve yorum: '{}'", userId, filmId, rating, comment.substring(0, Math.min(comment.length(), 50)) + "...");
        } else {
            log.info("Kullanıcı {} filme {} puan verdi: {}", userId, filmId, rating);
        }

        return getUserFilmInteractionStatus(userId, filmId);
    }

    @Override
    public UserFilmInteractionDTO toggleFavorite(String userId, String filmId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));

        if (!filmInfoRepository.existsById(filmId)) {
            throw new FilmNotFoundException("Film bulunamadı: " + filmId);
        }

        FilmPoint filmPoint = filmPointRepository.findByUserAndFilmId(user, filmId)
                .orElseGet(() -> FilmPoint.builder().user(user).filmId(filmId).build());

        filmPoint.setFilmFav(filmPoint.getFilmFav() == null || filmPoint.getFilmFav() == 0 ? 1 : 0);
        filmPointRepository.save(filmPoint);
        log.info("Kullanıcı {} film {} favori durumu: {}", userId, filmId, (filmPoint.getFilmFav() != null && filmPoint.getFilmFav() == 1));

        return getUserFilmInteractionStatus(userId, filmId);
    }

    @Override
    public UserFilmInteractionDTO addComment(String userId, String filmId, String comment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));

        if (!filmInfoRepository.existsById(filmId)) {
            throw new FilmNotFoundException("Film bulunamadı: " + filmId);
        }

        FilmPoint filmPoint = filmPointRepository.findByUserAndFilmId(user, filmId)
                .orElseGet(() -> FilmPoint.builder().user(user).filmId(filmId).build());

        filmPoint.setComment(comment);
        filmPointRepository.save(filmPoint);
        log.info("Kullanıcı {} filme {} yorum ekledi: '{}'", userId, filmId, 
                comment != null ? comment.substring(0, Math.min(comment.length(), 50)) + "..." : "null");

        return getUserFilmInteractionStatus(userId, filmId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserFilmInteractionDTO getUserFilmInteractionStatus(String userId, String filmId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));

        // Film var mı kontrolü. Eğer film yoksa, filmService.getAverageRatingForFilm zaten hata fırlatacaktır.
        // Bu yüzden filmInfoRepository.existsById(filmId) kontrolü burada loglama veya özel bir DTO yanıtı için yapılabilir.
        // Eğer film bulunamazsa, FilmNotFoundException fırlatılması daha tutarlı olabilir.
        if (!filmInfoRepository.existsById(filmId)) {
             log.warn("Etkileşim durumu sorgulanan film ({}) bulunamadı.", filmId);
             throw new FilmNotFoundException("Film bulunamadı: " + filmId); // Film yoksa hata fırlatmak daha doğru.
        }

        FilmPoint filmPoint = filmPointRepository.findByUserAndFilmId(user, filmId).orElse(null);
        BigDecimal averageRating = filmService.getAverageRatingForFilm(filmId); // Bu metot film yoksa hata fırlatır.
        Long totalRatings = filmPointRepository.countRatingsByFilmId(filmId); // Toplam puan sayısını al

        return UserFilmInteractionDTO.builder()
                .filmId(filmId)
                .userRating(filmPoint != null ? filmPoint.getFilmPoint() : null)
                .userComment(filmPoint != null ? filmPoint.getComment() : null)
                .isFavorite(filmPoint != null && filmPoint.getFilmFav() != null && filmPoint.getFilmFav() == 1)
                .averageRating(averageRating)
                .totalRatings(totalRatings) // Toplam puan sayısını ekle
                // .isInWatchlist(false) // Bu alan "izleme listesi" özelliğiyle gelecek
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FilmSummaryDTO> getUserFavoriteFilms(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));

        List<FilmPoint> favoriteFilmPoints = filmPointRepository.findAllByUserAndFilmFav(user, 1);

        if (favoriteFilmPoints.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> filmIds = favoriteFilmPoints.stream()
                .map(FilmPoint::getFilmId)
                .distinct()
                .collect(Collectors.toList());

        List<FilmInfo> filmInfos = filmInfoRepository.findAllById(filmIds);

        return filmInfos.stream()
                .map(filmInfo -> FilmSummaryDTO.builder()
                        .id(filmInfo.getId())
                        .title(filmInfo.getName())
                        .imageUrl(generateImageUrl(filmInfo.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatedFilmDTO> getLatestRatedFilms(String userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));

        // lastUpd null olabileceği için ve yeni kayıtlarda created ile aynı olacağı için
        // önce lastUpd (varsa), sonra created'a göre sıralamak daha doğru olabilir.
        // Veya FilmPoint entity'sinde @UpdateTimestamp ile lastUpd'nin her zaman dolu olmasını sağlayın.
        // Şimdiki FilmPointRepository metodumuz OrderByLastUpdDescCreatedDesc şeklinde.
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "lastUpd", "created"));

        List<FilmPoint> ratedFilmPoints = filmPointRepository.findRatedFilmsByUserOrderByLatest(user, pageable);

        if (ratedFilmPoints.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> filmIds = ratedFilmPoints.stream()
                .map(FilmPoint::getFilmId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, FilmInfo> filmInfoMap = filmInfoRepository.findAllById(filmIds).stream()
                .collect(Collectors.toMap(FilmInfo::getId, fi -> fi));

        return ratedFilmPoints.stream()
                .map(fp -> {
                    FilmInfo filmInfo = filmInfoMap.get(fp.getFilmId());
                    if (filmInfo == null) {
                        log.warn("Puanlanmış film için FilmInfo bulunamadı: filmId={}, pointId={}", fp.getFilmId(), fp.getPointId());
                        return null;
                    }
                    FilmSummaryDTO filmSummary = FilmSummaryDTO.builder()
                            .id(filmInfo.getId())
                            .title(filmInfo.getName())
                            .imageUrl(generateImageUrl(filmInfo.getId()))
                            .build();
                    return RatedFilmDTO.builder()
                            .film(filmSummary)
                            .userRating(fp.getFilmPoint())
                            .userComment(fp.getComment())
                            // En güncel tarihi al: lastUpd doluysa onu, değilse created'ı kullan
                            .ratedDate(fp.getLastUpd() != null ? fp.getLastUpd() : fp.getCreated())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FilmReviewDTO> getFilmReviews(String filmId) {
        if (!filmInfoRepository.existsById(filmId)) {
            throw new FilmNotFoundException("Film bulunamadı: " + filmId);
        }

        // Bu filme yapılan tüm yorumları ve puanlamaları getir (comment veya rating olan kayıtlar)
        List<FilmPoint> filmPoints = filmPointRepository.findAllByFilmIdAndCommentOrRatingExists(filmId);

        return filmPoints.stream()
                .filter(fp -> fp.getComment() != null || fp.getFilmPoint() != null)
                .map(fp -> FilmReviewDTO.builder()
                        .id(fp.getPointId())
                        .user(UserSummaryDTO.builder()
                                .id(fp.getUser().getId())
                                .name(fp.getUser().getName())
                                .build())
                        .rating(fp.getFilmPoint())
                        .text(fp.getComment())
                        .created(fp.getCreated())
                        .likes(0) // İleride beğeni sistemi için
                        .build())
                .sorted((r1, r2) -> r2.getCreated().compareTo(r1.getCreated())) // En yeni önce
                .collect(Collectors.toList());
    }

    private String generateImageUrl(String filmId) {
        return "http://localhost:8080/api/v1/films/image/" + filmId; // Kendi URL yapınıza göre güncelleyin
    }
}