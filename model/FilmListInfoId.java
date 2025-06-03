package com.example.moodmovies.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode // Bileşik anahtarlar için @EqualsAndHashCode önemlidir.
public class FilmListInfoId implements Serializable {

    private static final long serialVersionUID = 1L; // Serializable arayüzü için standart.

    @Column(name = "LIST_ID", length = 15, nullable = false)
    private String listId;

    @Column(name = "FILM_ID", length = 15, nullable = false)
    private String filmId;
}