package com.example.moodmovies.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Mevcut bir film listesinin bilgilerini güncelleme isteği için DTO.
 * Alanlar opsiyoneldir; sadece dolu gelenler güncellenir.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListUpdateRequestDTO {

    @Size(max = 100, message = "Liste adı en fazla 100 karakter olabilir.")
    private String name;

    @Size(max = 50, message = "Etiket en fazla 50 karakter olabilir.")
    private String tag; // Eğer etiket güncellenirse boş olmamalı (DB'de NOT NULL)

    private Integer visible;

    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir.")
    private String description;

    // private Integer status; // Opsiyonel: Liste durumunu (aktif/arşiv) güncellemek için
    // private Boolean isOrdered; // Opsiyonel: Sıralama tercihini güncellemek için
}