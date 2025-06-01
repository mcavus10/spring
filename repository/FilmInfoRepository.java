package com.example.moodmovies.repository;

import com.example.moodmovies.model.FilmInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FilmInfo entity'si için veritabanı erişim katmanı.
 * MOODMOVIES_ALL_FILMS_INFO görünümü üzerinde sorgulama yapmak için kullanılır.
 */
@Repository
public interface FilmInfoRepository extends JpaRepository<FilmInfo, String> {
    // Özel sorgu metotları gerekirse buraya eklenebilir.
    // Örneğin: List<FilmInfo> findByNameContainingIgnoreCase(String name);
    
    /**
     * Film adına göre arama yapar ve en fazla 5 eşleşen filmi döndürür.
     * Öneri listeleri için kullanılır.
     * @param name Aranacak film adı parçası
     * @return En fazla 5 filmden oluşan liste (FilmInfo nesneleri)
     */
    List<FilmInfo> findTop5ByNameContainingIgnoreCaseOrderById(String name); // ID'ye göre sıralama ekleyerek tutarlılık sağla
}
