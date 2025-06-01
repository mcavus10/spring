package com.example.moodmovies.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * MOODMOVIES_ALL_FILMS_INFO veritabanı görünümünü temsil eden Entity sınıfı.
 * Bu görünüm salt okunurdur, bu nedenle @Immutable olarak işaretlenmiştir.
 */
@Entity
@Table(name = "MOODMOVIES_ALL_FILMS_INFO")
@Immutable // Veritabanı görünümü olduğu için salt okunur
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FilmInfo {

    @Id
    @Column(name = "FILM_ID")
    private String id;

    @Column(name = "FILM_NAME")
    private String name;

    @Column(name = "FILM_RAYTING")
    private BigDecimal rating;

    @Column(name = "FILM_RELEASE_DATE")
    private LocalDate releaseDate;

    @Column(name = "FILM_COUNTRY")
    private String country;

    @Column(name = "RUNTIME")
    private Integer runtime;

    @Column(name = "PLOT")
    private String plot;

    @Lob
    @Column(name = "ImageByte", columnDefinition = "VARBINARY(MAX)")
    private byte[] imageByte;

    @Column(name = "TUR_1")
    private String tur1;

    @Column(name = "TUR_2")
    private String tur2;

    @Column(name = "TUR_3")
    private String tur3;

    @Column(name = "TUR_4")
    private String tur4;

    /**
     * Film bilgilerinin özet string temsili
     * @return Film kimliği ve adını içeren string
     */
    @Override
    public String toString() {
        return "FilmInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", rating=" + rating +
                ", releaseDate=" + releaseDate +
                ", country='" + country + '\'' +
                '}';
    }
}
