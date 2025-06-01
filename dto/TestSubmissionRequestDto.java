package com.example.moodmovies.dto;

import java.util.List;

/**
 * DTO representing the complete request body for submitting personality test answers.
 * Contains a list of individual answer submissions.
 */
public record TestSubmissionRequestDto(
    List<AnswerSubmissionDto> answers
) {}
