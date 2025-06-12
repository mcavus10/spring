package com.example.moodmovies.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarUpdateRequestDTO {
    @NotBlank(message = "Avatar ID boş olamaz.")
    private String avatarId;
} 