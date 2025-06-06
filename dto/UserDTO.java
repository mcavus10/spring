package com.example.moodmovies.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private String id;
    private String name;
    private String email;
    private String authType; // Kimlik doğrulama sağlayıcısını tanımlayan String (LOCAL, GOOGLE, FACEBOOK)
    private String providerId; // Dış sağlayıcılar için benzersiz ID
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;

    private Long ratingCount;    // Yaptığı puanlama sayısı
    private Long favoriteCount;  // Favoriye eklediği film sayısı
    private Long listCount;      // Oluşturduğu liste sayısı

}
