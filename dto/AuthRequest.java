package com.example.moodmovies.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthRequest {

    @NotBlank(message = "Email bou015f olamaz")
    @Email(message = "Geu00e7erli bir email adresi giriniz")
    private String email;

    @NotBlank(message = "u015eifre bou015f olamaz")
    @Size(min = 6, message = "u015eifre en az 6 karakter olmalu0131du0131r")
    private String password;
}
