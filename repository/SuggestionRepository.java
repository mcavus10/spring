package com.example.moodmovies.repository;

import com.example.moodmovies.model.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing Suggestion entities in the database.
 */
@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, String> {

    /**
     * Finds the latest film IDs suggested for a specific user.
     * Returns only the film IDs from the most recent suggestions.
     *
     * @param userId The ID of the user to get suggestions for
     * @return List of film IDs suggested for the user, ordered by creation date (newest first)
     */
    @Query("SELECT s.filmId FROM Suggestion s WHERE s.user.id = :userId ORDER BY s.created DESC")
    List<String> findLatestFilmIdsByUserId(@Param("userId") String userId);
}
