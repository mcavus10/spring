package com.example.moodmovies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDTO {
    @NotBlank(message = "Yorum boş olamaz.")
    @Size(max = 255, message = "Yorum en fazla 255 karakter olabilir.")
    private String comment;
} 