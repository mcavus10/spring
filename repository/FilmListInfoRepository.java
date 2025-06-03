package com.example.moodmovies.repository;

import com.example.moodmovies.model.FilmList;
import com.example.moodmovies.model.FilmListInfo;
import com.example.moodmovies.model.FilmListInfoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilmListInfoRepository extends JpaRepository<FilmListInfo, FilmListInfoId> { // Birincil anahtar FilmListInfoId (bileşik)

    /**
     * Belirli bir FilmList entity'sine ait tüm FilmListInfo kayıtlarını (yani listedeki tüm filmleri) getirir.
     * @param filmList Ana FilmList entity'si
     * @return O listeye ait FilmListInfo nesnelerinin listesi
     */
    List<FilmListInfo> findAllByFilmList(FilmList filmList);

    /**
     * Belirli bir liste ID'sine ait tüm FilmListInfo kayıtlarını getirir.
     * @param listId Liste ID'si
     * @return O listeye ait FilmListInfo nesnelerinin listesi
     */
    List<FilmListInfo> findAllByFilmList_ListId(String listId);

    /**
     * Belirli bir FilmList entity'sine ait film sayısını döndürür.
     * FilmListSummaryDTO oluştururken kullanılır.
     * @param filmList Ana FilmList entity'si
     * @return O listedeki film sayısı (long)
     */
    long countByFilmList(FilmList filmList);

    /**
     * Belirli bir listeden belirli bir film ID'sine sahip kaydı siler.
     * Bu, JpaRepository'deki deleteById(FilmListInfoId id) ile aynı işi yapar
     * ama bazen daha okunabilir olabilir veya özel bir silme mantığı gerektiğinde kullanılabilir.
     * Genellikle deleteById(id) yeterlidir.
     * @param listId Liste ID'si
     * @param filmId Film ID'si
     */
    @Modifying // Veritabanını değiştiren sorgular için (DELETE, UPDATE)
    @Query("DELETE FROM FilmListInfo fli WHERE fli.id.listId = :listId AND fli.id.filmId = :filmId")
    void deleteByListIdAndFilmId(@Param("listId") String listId, @Param("filmId") String filmId);
    // Not: Bu metot, servis katmanında yetkilendirme kontrolü yapıldıktan sonra çağrılmalıdır.
    // Alternatif olarak, servis katmanında önce FilmListInfo nesnesi bulunup sonra JpaRepository.delete(entity)
    // veya deleteById(id) çağrılabilir. FilmList entity'sindeki cascade ayarları da önemlidir.
    // Şu anki FilmList entity'sindeki @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    // ayarı, bir FilmListInfo'yu filmList.getFilmListInfos().remove(filmListInfo) yapıp
    // filmList'i kaydettiğinizde veya FilmList'i sildiğinizde FilmListInfo'ların da silinmesini sağlar.
    // Bu nedenle bu özel delete metodu her zaman gerekmeyebilir.
}