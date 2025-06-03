package com.example.moodmovies.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "MOODMOVIES_FILMS_LIST_INFO") // DB'deki PK tanımı: PRIMARY KEY (LIST_ID, FILM_ID)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilmListInfo {

    @EmbeddedId
    private FilmListInfoId id; // Bileşik birincil anahtar

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("listId") // FilmListInfoId içindeki 'listId' alanını bu ilişkiye map eder.
    @JoinColumn(name = "LIST_ID", referencedColumnName = "LIST_ID", nullable = false)
    private FilmList filmList;

    // Veritabanı script'inizde USER_ID kolonu ve MOODMOVIES_USERS'a FK olduğu için bu alanı ekliyoruz.
    // Bu User, filmList.getUser() ile aynı kullanıcı olmalıdır.
    // Bu tutarlılık servis katmanında sağlanacaktır.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    // filmId alanı FilmListInfoId içinde zaten mevcut (`id.getFilmId()` ile erişilir).
    // Eğer Film entity'sine doğrudan bir @ManyToOne ilişkiniz olsaydı,
    // @MapsId("filmId") ile o ilişki de buraya map edilebilirdi.
    // Ancak biz filmId'yi string olarak saklıyoruz.

    @CreationTimestamp
    @Column(name = "CREATED", nullable = false, updatable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "LAST_UPD")
    private LocalDateTime lastUpd;
}