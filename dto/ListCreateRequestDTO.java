package com.example.moodmovies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Yeni bir film listesi oluşturma isteği için DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListCreateRequestDTO {

    @NotBlank(message = "Liste adı boş bırakılamaz.")
    @Size(max = 100, message = "Liste adı en fazla 100 karakter olabilir.")
    private String name;

    @NotBlank(message = "Liste etiketi (tag) boş bırakılamaz.") // Veritabanında NOT NULL olduğu için
    @Size(max = 50, message = "Etiket en fazla 50 karakter olabilir.")
    private String tag;

    @NotNull(message = "Listenin görünürlük durumu (visible) belirtilmelidir.")
    private Integer visible; // Örneğin: 0 (Sadece Ben), 1 (Herkes)

    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir.")
    private String description;

    // Not: `isOrdered` alanı frontend'de vardı ama veritabanında `FilmList` tablosuna eklemedik.
    // Eğer bu özellik veritabanında saklanacaksa, buraya ve FilmList entity'sine eklenmeli.
    // private Boolean isOrdered;
}