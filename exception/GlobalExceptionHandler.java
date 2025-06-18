package com.example.moodmovies.exception;

import com.example.moodmovies.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(UserNotFoundException ex,
                                                                        HttpServletRequest request) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(RuntimeException ex,
                                                                   HttpServletRequest request) {
        System.err.println("Beklenmedik bir hata oluştu:"+ex.getMessage());
        ex.printStackTrace();

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Unexpected error , please try again another time ")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                        HttpServletRequest request) {
        String message = "Veri bütünlüğü hatası oluştu. Lütfen girdiğiniz bilgileri kontrol edin.";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Throwable root = ex.getRootCause();
        if (root != null && root.getMessage() != null) {
            String rootMsg = root.getMessage();
            if (rootMsg.contains("UQ_MOODMOVIES_FORUM_TITLE")) {
                message = "Bu başlık zaten mevcut. Lütfen farklı bir başlık seçin.";
                status = HttpStatus.CONFLICT;
            } else if (rootMsg.contains("FK_RESPONSE_QUESTION")) {
                message = "Gönderilen test sorusu geçersiz. Lütfen testi yeniden başlatın.";
                status = HttpStatus.BAD_REQUEST;
            }
        }
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, status);
    }
}
