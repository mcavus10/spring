package com.example.moodmovies.service;

import com.example.moodmovies.dto.AnswerSubmissionDto;
import com.example.moodmovies.exception.UserNotFoundException;

import java.util.List;

/**
 * Service interface for handling personality test submissions.
 */
public interface TestSubmissionService {
    
    /**
     * Saves a user's responses to the personality test.
     *
     * @param userId The ID of the user submitting the test.
     * @param submittedAnswers List of answer submissions containing question and answer IDs.
     * @throws UserNotFoundException if the user with the provided ID does not exist.
     */
    void saveUserResponses(String userId, List<AnswerSubmissionDto> submittedAnswers) throws UserNotFoundException;
}
