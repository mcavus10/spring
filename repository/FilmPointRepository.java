package com.example.moodmovies.repository;

import com.example.moodmovies.model.FilmPoint;
import com.example.moodmovies.model.User;
import org.springframework.data.domain.Pageable; // Sayfalama ve sıralama için
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilmPointRepository extends JpaRepository<FilmPoint, String> {

    /**
     * Belirli bir kullanıcı ve film ID'sine ait puan/favori kaydını bulur.
     */
    Optional<FilmPoint> findByUserAndFilmId(User user, String filmId);

    /**
     * Belirli bir kullanıcının favorilediği (filmFav = 1) tüm FilmPoint kayıtlarını getirir.
     */
    List<FilmPoint> findAllByUserAndFilmFav(User user, Integer filmFavValue);

    /**
     * Belirli bir kullanıcının puan verdiği (filmPoint alanı NULL olmayan) tüm FilmPoint kayıtlarını getirir.
     * (Bu metot genel bir listeleme için tutulabilir, son N tanesi için aşağıdaki metot kullanılır)
     */
    List<FilmPoint> findAllByUserAndFilmPointIsNotNull(User user);

    /**
     * Belirli bir film ID'sine sahip filmin, tüm kullanıcılar tarafından verilen puanların ortalamasını hesaplar.
     */
    @Query("SELECT AVG(fp.filmPoint) FROM FilmPoint fp WHERE fp.filmId = :filmId AND fp.filmPoint IS NOT NULL")
    Optional<Double> findAverageRatingByFilmId(@Param("filmId") String filmId);

    /**
     * Belirli bir film ID'sine sahip filmin toplam puan sayısını hesaplar.
     */
    @Query("SELECT COUNT(fp.filmPoint) FROM FilmPoint fp WHERE fp.filmId = :filmId AND fp.filmPoint IS NOT NULL")
    Long countRatingsByFilmId(@Param("filmId") String filmId);

    /**
     * Belirli bir kullanıcının puan verdiği filmleri (filmPoint alanı NULL olmayan),
     * son güncelleme (`lastUpd`) tarihine göre azalan sırada (en yeni önce) getirir.
     * Pageable parametresi sayesinde sonuçlar limitlenebilir.
     */
    @Query("SELECT fp FROM FilmPoint fp WHERE fp.user = :user AND fp.filmPoint IS NOT NULL")
    List<FilmPoint> findRatedFilmsByUserOrderByLatest(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT fp.filmId, COUNT(fp.pointId) as favoriteCount FROM FilmPoint fp WHERE fp.filmFav = 1 GROUP BY fp.filmId ORDER BY favoriteCount DESC")
    List<Object[]> findTopFavoritedFilmIds(Pageable pageable);

    // Belirtilen kullanıcı listesi için her bir kullanıcının toplam puanlama sayısını döndürür.
    @Query("SELECT fp.user.id, COUNT(fp.pointId) FROM FilmPoint fp WHERE fp.user.id IN :userIds AND fp.filmPoint IS NOT NULL GROUP BY fp.user.id")
    List<Object[]> countRatingsByUserIds(@Param("userIds") List<String> userIds);

    // Belirtilen kullanıcı listesi için her bir kullanıcının toplam favori sayısını döndürür.
    @Query("SELECT fp.user.id, COUNT(fp.pointId) FROM FilmPoint fp WHERE fp.user.id IN :userIds AND fp.filmFav = 1 GROUP BY fp.user.id")
    List<Object[]> countFavoritesByUserIds(@Param("userIds") List<String> userIds);

    // Belirli bir filme yapılan tüm yorumları ve puanlamaları getirir (comment veya rating olan kayıtlar)
    @Query("SELECT fp FROM FilmPoint fp WHERE fp.filmId = :filmId AND (fp.comment IS NOT NULL OR fp.filmPoint IS NOT NULL) ORDER BY fp.created DESC")
    List<FilmPoint> findAllByFilmIdAndCommentOrRatingExists(@Param("filmId") String filmId);
}