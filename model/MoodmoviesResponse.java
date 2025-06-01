package com.example.moodmovies.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * Entity representing a user's response to a personality test question.
 * Maps to the MOODMOVIES_RESPONSE table in the database.
 */
@Entity
@Table(name = "MOODMOVIES_RESPONSE")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoodmoviesResponse {

    @Id
    @GeneratedValue(generator = "response_id_generator")
    @GenericGenerator(
        name = "response_id_generator",
        strategy = "com.example.moodmovies.config.ResponseIdGenerator"
    )
    @Column(name = "RESPONSE_ID", length = 15)
    private String responseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "QUESTION_ID", nullable = false, length = 15)
    private String questionId;

    @Column(name = "ANSWER_ID", nullable = false, length = 15)
    private String answerId;

    @CreationTimestamp
    @Column(name = "RESPONSE_DATE", nullable = false)
    private LocalDateTime responseDate;
}
