package com.example.moodmovies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDTO {
    
    @NotBlank(message = "İsim boş olamaz.")
    @Size(min = 2, max = 50, message = "İsim 2-50 karakter arasında olmalıdır.")
    private String name;
    
    // İleride email güncelleme, profil resmi vs. eklenebilir
    // private String email;
    // private String profilePictureUrl;
} 