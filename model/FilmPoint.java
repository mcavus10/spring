package com.example.moodmovies.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "MOODMOVIES_FILMS_POINT", uniqueConstraints = {
    // Bu UNIQUE constraint'i veritabanında ALTER TABLE ile eklediğiniz için
    // burada tekrar belirtmek isteğe bağlıdır ama JPA'nın da bilmesi iyi olur.
    // Eğer DB'de varsa, JPA bunu zaten dikkate alacaktır.
    // uniqueConstraint isimleri DB'deki ile aynı olabilir veya JPA kendi isimlendirmesini yapar.
    @UniqueConstraint(name = "UQ_User_Film_Point_JPA", columnNames = {"USER_ID", "FILM_ID"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilmPoint {

    @Id
    @GeneratedValue(generator = "point_id_generator")
    @GenericGenerator(
        name = "point_id_generator",
        strategy = "com.example.moodmovies.config.PointIdGenerator" // Tam yolu belirtin
    )
    @Column(name = "POINT_ID", length = 15)
    private String pointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "FILM_ID", length = 15, nullable = false)
    private String filmId; // MOODMOVIES_FILMS.FILM_ID'ye referans

    @Min(value = 1, message = "Puan en az 1 olmalıdır")
    @Max(value = 10, message = "Puan en fazla 10 olmalıdır") // Puanlama skalanız 1-10 ise
    @Column(name = "FILM_POINT")
    private Integer filmPoint; // Kullanıcı sadece favorileyip puan vermeyebilir (NULL olabilir)

    @Column(name = "FILM_FAV") // 0: Favori değil, 1: Favori
    private Integer filmFav;   // Kullanıcı sadece puanlayıp favorilemeyebilir (NULL olabilir)

    @Column(name = "COMMENT", length = 255) // Kullanıcının film hakkındaki yorumu
    private String comment;    // Yorum isteğe bağlı (NULL olabilir)

    @CreationTimestamp
    @Column(name = "CREATED", nullable = false, updatable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "LAST_UPD")
    private LocalDateTime lastUpd;
}