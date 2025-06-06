package com.example.moodmovies.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForumCommentCreateDTO {
    @NotBlank(message = "Yorum içeriği boş olamaz.")
    private String comment;
}