package com.example.moodmovies.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kimlik doğrulama yanıtını için DTO nesnesi.
 * Token'lar HTTP-only cookie olarak geri döndürüldüğü için bu DTO'da bulunmazlar.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private Long expiresInMs;
    private UserDTO user;
}
