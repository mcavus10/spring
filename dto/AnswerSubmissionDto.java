package com.example.moodmovies.dto;

/**
 * DTO representing a single answer submission from the personality test.
 * Contains the question ID and the selected answer ID.
 */
public record AnswerSubmissionDto(
    String questionId,
    String answerId
) {}
