package com.example.moodmovies.repository;

import com.example.moodmovies.model.FilmList;
import com.example.moodmovies.model.User;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilmListRepository extends JpaRepository<FilmList, String> {

    List<FilmList> findAllByUser(User user);

    // Kullanıcının belirli bir durumdaki listelerini getirir (örn: sadece aktif olanlar)
    List<FilmList> findAllByUserAndStatus(User user, Integer status);

    // Kullanıcının belirli bir görünürlük ve durumdaki listelerini getirir
    List<FilmList> findAllByUserAndVisibleAndStatus(User user, Integer visibility, Integer status);

    Optional<FilmList> findByListIdAndUser(String listId, User user);

    // Herkese açık ve aktif olan listeleri, oluşturulma tarihine göre en yeniden eskiye doğru getirir.
    List<FilmList> findByVisibleAndStatusOrderByCreatedDesc(Integer visible, Integer status, Pageable pageable);

    @Query("SELECT fl.user.id, COUNT(fl.listId) FROM FilmList fl WHERE fl.user.id IN :userIds AND fl.status = 1 GROUP BY fl.user.id")
    List<Object[]> countListsByUserIds(@Param("userIds") List<String> userIds);
}