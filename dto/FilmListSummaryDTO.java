package com.example.moodmovies.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmListSummaryDTO {
    private String listId;
    private String name;
    private String tag;
    private int filmCount;
    private Integer visibility;
    private UserSummaryDTO owner; // Hatanın çözümü için bu satır eklendi
}