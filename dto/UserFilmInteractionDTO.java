package com.example.moodmovies.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class UserFilmInteractionDTO {
    private String filmId;
    private Integer userRating;       // Kullanıcının filme verdiği puan (null olabilir)
    private Boolean isFavorite;       // Kullanıcının filmi favorileyip favorilemediği
    private BigDecimal averageRating; // Filmin genel ortalama puanı
    // private Boolean isInWatchlist; // İzleme listesi özelliği eklendiğinde aktifleşecek
}