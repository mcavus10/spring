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
     * Belirli bir kullanıcının puan verdiği filmleri (filmPoint alanı NULL olmayan),
     * son güncelleme (`lastUpd`) veya oluşturulma (`created`) tarihine göre azalan sırada (en yeni önce) getirir.
     * Pageable parametresi sayesinde sonuçlar limitlenebilir.
     */
    List<FilmPoint> findByUserAndFilmPointIsNotNullOrderByLastUpdDescCreatedDesc(User user, Pageable pageable);
    // Metot adını güncelledim: OrderByLastUpdDescCreatedDesc, lastUpd null ise created'a göre sıralar.
}