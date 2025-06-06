package com.example.moodmovies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForumPostCreateDTO {
    @NotBlank(message = "Başlık boş olamaz.")
    @Size(max = 255, message = "Başlık 255 karakteri geçemez.")
    private String title;

    @Size(max = 50, message = "Etiket 50 karakteri geçemez.")
    private String tag; // İsteğe bağlı olabilir

    @NotBlank(message = "İçerik boş olamaz.")
    private String context;
}