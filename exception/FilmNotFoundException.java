package com.example.moodmovies.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Film bulunamadığında fırlatılan özel exception.
 * HTTP 404 (Not Found) durum kodu ile eşleştirilir.
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // 404 hatası döndürmesi için
public class FilmNotFoundException extends RuntimeException {
    public FilmNotFoundException(String message) {
        super(message);
    }
}
