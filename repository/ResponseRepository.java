package com.example.moodmovies.repository;

import com.example.moodmovies.model.MoodmoviesResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for managing MoodmoviesResponse entities in the database.
 */
@Repository
public interface ResponseRepository extends JpaRepository<MoodmoviesResponse, String> {
    
    /**
     * Deletes all responses for a given user ID.
     *
     * @param userId The ID of the user whose responses are to be deleted.
     * @return The number of responses deleted.
     */
    @Modifying
    @Transactional
    long deleteByUser_Id(String userId);
}
