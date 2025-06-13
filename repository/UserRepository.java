package com.example.moodmovies.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.moodmovies.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * E-posta adresine göre kullanıcı arar
     * @param email Aranacak e-posta adresi
     * @return Bulunan kullanıcı veya boş Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * Sağlayıcı ID'sine göre kullanıcı arar (Google, Facebook vb.)
     * @param providerId Sağlayıcı ID'si 
     * @return Bulunan kullanıcı veya boş Optional
     */
    Optional<User> findByProviderId(String providerId);

    /**
     * İsmine göre kullanıcı arar (büyük-küçük harf duyarsız)
     * @param partialName Aranacak ismin bölümü
     * @return Bulunan kullanıcılar listesi
     */
    List<User> findByNameContainingIgnoreCase(String partialName);

    /**
     * En çok puanlama yapan kullanıcıları getiren sorgu
     */
    @Query("SELECT u, COUNT(p.pointId) as interactionCount FROM User u JOIN FilmPoint p ON u.id = p.user.id GROUP BY u ORDER BY interactionCount DESC")
    List<Object[]> findTopReviewers(Pageable pageable);

    // YENİ METOT: Avatar'ı olmayan kullanıcıları döndürür
    List<User> findByAvatarIdIsNull();

}
