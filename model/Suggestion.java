package com.example.moodmovies.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a film suggestion for a user.
 * Maps to the MOODMOVIES_SUGGEST table in the database.
 */
@Entity
@Table(name = "MOODMOVIES_SUGGEST")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Suggestion {

    @Id
    @Column(name = "SUGGEST_ID")
    private String suggestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "FILM_ID", nullable = false)
    private String filmId;

    @CreationTimestamp
    @Column(name = "CREATED", nullable = false)
    private LocalDateTime created;
}
