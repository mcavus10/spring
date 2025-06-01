package com.example.moodmovies.exception;

/**
 * Exception thrown when there's an error communicating with the AI service.
 */
public class AiServiceException extends RuntimeException {
    
    public AiServiceException(String message) {
        super(message);
    }
    
    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
