package com.example.moodmovies.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryDTO {
    private String id;
    private String name;
    // private String profilePictureUrl; // Opsiyonel
    private String avatarImageUrl;
}